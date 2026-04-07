package com.inventoryservice.ServiceImpl;

import com.inventoryservice.DTO.Request.ServiceRequest;
import com.inventoryservice.DTO.Request.productRequestDto;
import com.inventoryservice.DTO.Response.InventoryResponse;
import com.inventoryservice.DTO.Response.ProductResponseDto;
import com.inventoryservice.Entity.Product;
import com.inventoryservice.Entity.Reservation;
import com.inventoryservice.Enum.ReservationStatus;
import com.inventoryservice.Enum.StockStatus;
import com.inventoryservice.Feign.NotificationService;
import com.inventoryservice.Repository.ProductRepository;
import com.inventoryservice.Repository.ReservationRepository;
import com.inventoryservice.Service.InventoryService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Slf4j
@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReservationRepository reservationRepository;
    private static final ExecutorService EMAIL_EXECUTOR = Executors.newFixedThreadPool(5);

    @Transactional
    public InventoryResponse reserveStock(ServiceRequest request) {
        log.info("reserveStock method called for: {}",request);
        InventoryResponse response = new InventoryResponse();

        response.setRespondedTime(LocalDateTime.now());

        Reservation reservation = new Reservation();
        reservation.setWorkFlowStepId(request.getWorkFlowStepId());
        reservation.setReservationId(UUID.randomUUID().toString());
        response.setTransactionId(reservation.getReservationId());

        reservation.setWorkflowId(request.getWorkflowId());
        reservation.setUserId(request.getUserId());
        reservation.setQuantity(request.getQuantity());
        try {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            reservation.setProduct(product);
            response.setAmount(product.getPrice() * request.getQuantity());

            if (product.getStockQuantity() < request.getQuantity()
                    || product.getStatus() == StockStatus.OUT_OF_STOCK) {
                reservation.setStatus(ReservationStatus.FAILED);
                reservationRepository.save(reservation);
                response.setStatus("FAILED");
                response.setErrorMessage("Insufficient stock");

                //notificationService.sendNotificationForFailedOutOfStock(request)
//                Thread t=new Thread(() -> notificationService.sendNotificationForFailedOutOfStock(request));
//                t.start();
                EMAIL_EXECUTOR.submit(() ->{
                    try{
                        String string = notificationService.sendNotificationForFailedOutOfStock(request);
                        log.info("reserveStock for {} FAILED DUE TO OUT OF STOCK OR LOW STACK THEN BOOKED and Mail SUCCESS: {}",request,string);
                    } catch (Exception e) {
                        log.info("reserveStock for {} FAILED DUE TO OUT OF STOCK OR LOW STACK THEN BOOKED and Mail FAILED: {}",request,e.getMessage());
                    } });
                return response;
            }

            product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
            if (product.getStockQuantity() == 0) {
                product.setStatus(StockStatus.OUT_OF_STOCK);
            } else {
                product.setStatus(StockStatus.AVAILABLE);
            }

            productRepository.save(product);
            reservation.setStatus(ReservationStatus.RESERVED);
            reservationRepository.save(reservation);
            response.setStatus("SUCCESS");
            log.info("reserveStock SUCCESS for: {}",request);
            return response;

        } catch (Exception e) {
            reservation.setStatus(ReservationStatus.FAILED);
            reservationRepository.save(reservation);
            response.setStatus("FAILED");
            response.setErrorMessage(e.getMessage());
            log.error("reserveStock for {} FAILED duo to: {}",request,e.getMessage());
            return response;
        }
    }

    @Transactional
    public InventoryResponse releaseStock(Long workflowId) {
        log.info("releaseStock method called for: {}",workflowId);
        InventoryResponse response = new InventoryResponse();
        response.setRespondedTime(LocalDateTime.now());

        Reservation reservation = reservationRepository.findByWorkflowId(workflowId);
        if (reservation == null) {
            throw new RuntimeException("Reservation not found for workflowId: " + workflowId);
        }else if(reservation.getStatus() == ReservationStatus.RESERVED){
            Product product = reservation.getProduct();
            product.setStockQuantity(product.getStockQuantity() + reservation.getQuantity());
            product.setStatus(StockStatus.AVAILABLE);
            productRepository.save(product);
            reservation.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(reservation);
            log.info("Stock release Successfully for: {}",workflowId);
            response.setStatus("COMPENSATED");
            response.setTransactionId(reservation.getReservationId());
            response.setAmount(reservation.getQuantity() * product.getPrice());
            return response;
        }else{
            log.info("Stock is failed to release for: {}",workflowId);
            throw new RuntimeException("PRODUCT NOT RESERVED EXCEPTION");
        }
    }
    public String getReservationDetails(String reservationId) {
        log.info("getReservationDetails method called for: {}",reservationId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        return reservation.toString();
    }
    @Cacheable(value = "Products",key="#productId")
    public ProductResponseDto getProductDetail(Long productId) {
        log.info("getProductDetail method called for: {}",productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return productToProductResponseDto(product);
    }
    @CachePut(value = "Products",key="#result.productId")
    public ProductResponseDto createNewProduct(productRequestDto request) {
        log.info("createNewProduct method called for: {}",request);
        Product product=productRequestDtoToProduct(request);
        product= productRepository.save(product);
        return productToProductResponseDto(product);
    }
    @CacheEvict(value = "Products",key="#productId")
    public ProductResponseDto deleteProductDetail(Long productId) {
        log.info("deleteProductDetail method called for: {}",productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.deleteById(productId);
        return productToProductResponseDto(product);
    }

    @Override
    @CachePut(value = "Products",key="#result.productId")
    public ProductResponseDto updateProduct(productRequestDto request, long productId) {
        log.info("updateProduct method called for: {}",productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStatus(StockStatus.valueOf(request.getStatus()));
        product.setStockQuantity(request.getStockQuantity());
        product=productRepository.save(product);
        return productToProductResponseDto(product);
    }

    private ProductResponseDto productToProductResponseDto(Product product){
        log.info("productToProductResponseDto method called for: {}",product);
        ProductResponseDto productResponseDto=new ProductResponseDto();
        productResponseDto.setProductId(product.getProductId());
        productResponseDto.setProductName(product.getProductName());
        productResponseDto.setPrice(product.getPrice());
        productResponseDto.setStockQuantity(product.getStockQuantity());
        productResponseDto.setStatus(product.getStatus());
        productResponseDto.setUpdatedAt(product.getUpdatedAt());
        productResponseDto.setDescription(product.getDescription());
        return productResponseDto;
    }
    private Product productRequestDtoToProduct(productRequestDto request){
        log.info("productRequestDtoToProduct method called for: {}",request);
        Product p=new Product();
        p.setProductName(request.getProductName());
        p.setStatus(StockStatus.valueOf(request.getStatus()));
        p.setStockQuantity(request.getStockQuantity());
        p.setPrice(request.getPrice());
        p.setDescription(request.getDescription());
        return p;
    }
}