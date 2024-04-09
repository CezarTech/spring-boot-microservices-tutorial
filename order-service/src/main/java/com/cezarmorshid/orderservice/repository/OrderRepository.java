package com.cezarmorshid.orderservice.repository;

import com.cezarmorshid.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {

}
