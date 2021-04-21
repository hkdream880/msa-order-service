package com.example.orderservice.service;

import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.jpa.OrderRepository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Service
@Slf4j
public class OrderServiceImpl implements OrderService{
	
	OrderRepository orderRepository; 
	
	@Autowired
	public OrderServiceImpl(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}
	
	@Override
	public OrderDto createOrder(OrderDto orderDto) {
		// TODO Auto-generated method stub
		
		orderDto.setOrderId(UUID.randomUUID().toString());
		orderDto.setTotalPrice(orderDto.getQty() * orderDto.getUnitPrice());
		
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		OrderEntity orderEntity = mapper.map(orderDto, OrderEntity.class);
		
		orderRepository.save(orderEntity);
				
		OrderDto returnUserDto = mapper.map(orderEntity, OrderDto.class);
		
		return returnUserDto;
	}
	
	@Override
	public OrderDto getOrderByOrderId(String orderId) {
		// TODO Auto-generated method stub
		OrderEntity orderEntity = orderRepository.findByOrderId(orderId);
		OrderDto orderDto = new ModelMapper().map(orderEntity, OrderDto.class); 
		return orderDto;
	}
	
	@Override
	public Iterable<OrderEntity> getOrdersByUserId(String userId) {
		// TODO Auto-generated method stub
		return orderRepository.findByUserId(userId);
	}
	
	 
}
