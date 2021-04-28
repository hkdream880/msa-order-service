package com.example.orderservice.controller;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.messageque.KafkaProducer;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;

@RestController
@RequestMapping("/order-service")
public class OrderController {

	Environment env;
	OrderService orderService;
	KafkaProducer kafkaProducer;
	
	public OrderController(Environment env, OrderService orderService, KafkaProducer kafkaProducer) {
		this.env = env;
		this.orderService = orderService;
		this.kafkaProducer = kafkaProducer;
	}
	
	@GetMapping("/health-check")
	public String status() {
		return String.format("Order Service on port %s", env.getProperty("local.server.port"));
	}
	
	@PostMapping("/{userId}/orders")
	public ResponseEntity<ResponseOrder> createUser(@RequestBody RequestOrder requestOrder, @PathVariable("userId") String userId) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		
		OrderDto orderDto = mapper.map(requestOrder, OrderDto.class);
		
		orderDto.setUserId(userId);
		OrderDto createdOrderDto = orderService.createOrder(orderDto);
		
		ResponseOrder responseOrder = mapper.map(createdOrderDto, ResponseOrder.class);
		
		/* kafka message send*/
		kafkaProducer.send("example-catalog-topic", createdOrderDto);
		
		
		return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
	}
	
	@GetMapping("/{userId}/orders")
	public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable("userId") String userId) {
		
		Iterable<OrderEntity> orderList = orderService.getOrdersByUserId(userId);
		
		List<ResponseOrder> result = new ArrayList<ResponseOrder>();
		
		orderList.forEach(v -> {
			result.add(new ModelMapper().map(v, ResponseOrder.class));
		});
		
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
}
