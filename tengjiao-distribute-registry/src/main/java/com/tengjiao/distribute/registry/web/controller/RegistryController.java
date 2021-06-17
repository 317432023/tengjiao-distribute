package com.tengjiao.distribute.registry.web.controller;

import com.tengjiao.distribute.registry.model.Registry;
import com.tengjiao.distribute.registry.result.ReturnT;
import com.tengjiao.distribute.registry.service.RegistryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @author
 */
@Controller
@RequestMapping("/registry")
public class RegistryController {

    private RegistryService registryService;

    public RegistryController(RegistryService registryService) {
        this.registryService = registryService;
    }

    @RequestMapping("")
    public String index(Model model){
        return "registry/registry.index";
    }

    @RequestMapping("/pageList")
    @ResponseBody
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
                                        @RequestParam(required = false, defaultValue = "10") int length,
                                        String biz,
                                        String env,
                                        String key){
        return registryService.pageList(start, length, biz, env, key);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public ReturnT<String> delete(int id){
        return registryService.delete(id);
    }

    @RequestMapping("/update")
    @ResponseBody
    public ReturnT<String> update(Registry xxlRpcRegistry){
        return registryService.update(xxlRpcRegistry);
    }

    @RequestMapping("/add")
    @ResponseBody
    public ReturnT<String> add(Registry xxlRpcRegistry){
        return registryService.add(xxlRpcRegistry);
    }




}
