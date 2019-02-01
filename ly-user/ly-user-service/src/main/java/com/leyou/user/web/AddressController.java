package com.leyou.user.web;

import com.leyou.pojo.AddressDTO;
import com.leyou.user.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("addr")
public class AddressController {

    @Autowired
    private AddressService addressService;
    /**
     * 根据id查询某个收货人地址
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<AddressDTO> queryAddressById(@PathVariable("id") Long id){
        return ResponseEntity.ok(addressService.queryAddressById(id));
    }
}