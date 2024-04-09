package com.cezarmorshid.orderservice.service;

import brave.Tracer;
import com.cezarmorshid.orderservice.dto.InventoryResponse;
import com.cezarmorshid.orderservice.dto.OrderLineItemsDto;
import com.cezarmorshid.orderservice.dto.OrderRequest;
import com.cezarmorshid.orderservice.event.OrderPlacedEvent;
import com.cezarmorshid.orderservice.model.Order;
import com.cezarmorshid.orderservice.model.OrderLineItems;
import com.cezarmorshid.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    private final KafkaTemplate<String,OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest){
     Order order=   new Order();
     order.setOrderNumber(UUID.randomUUID().toString());

    List<OrderLineItems> orderLineItems=  orderRequest.getOrderLineItemsDtoList()
             .stream()
             .map(this::mapToDTo)
             .toList();

    order.setOrderLineItemsList(orderLineItems);

   List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();
    //Call Inventory service and place order if product is in
   InventoryResponse[] inventoryResponsArray= webClientBuilder.build().get()
            .uri("http://inventory-service/api/inventory",uriBuilder -> uriBuilder.queryParam("skuCode",skuCodes).build())
                    .retrieve()
                            .bodyToMono(InventoryResponse[].class)
                                    .block();

 boolean allProductsInStock=  Arrays.stream(inventoryResponsArray).allMatch(InventoryResponse::isInStock);

   if(allProductsInStock){
       orderRepository.save(order);
       kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
       return "Order Placed Successfully";
   }else{
       throw new IllegalArgumentException("Product is not in stock,please try again later");
   }


    }
    private OrderLineItems mapToDTo(OrderLineItemsDto orderLineItemsDto){
      OrderLineItems orderLineItems =  new OrderLineItems();
      orderLineItems.setPrice(orderLineItemsDto.getPrice());
      orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
      orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());

      return orderLineItems;
    }
}
