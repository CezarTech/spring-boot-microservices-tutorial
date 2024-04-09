package com.cezarmorshid.inventoryservice.service;

import com.cezarmorshid.inventoryservice.dto.InventoryResponse;
import com.cezarmorshid.inventoryservice.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {


    private final InventoryRepository inventoryRepository;
    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCode){

      return  inventoryRepository.findBySkuCodeIn(skuCode).stream()
              .map(inventory ->
                  InventoryResponse.builder()
                          .skuCode(inventory.getSkuCode())
                          .isInStock(inventory.getQuantity()>0)
                          .build()
              ).toList();
    }


}
