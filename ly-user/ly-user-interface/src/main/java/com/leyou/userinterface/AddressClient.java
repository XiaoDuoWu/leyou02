package com.leyou.userinterface;

import com.leyou.pojo.AddressDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "user-service",path = "addr")
public interface AddressClient {
    @GetMapping("{id}")
    AddressDTO queryAddressById(@PathVariable("id") Long id);
}
