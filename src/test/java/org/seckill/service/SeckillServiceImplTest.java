package org.seckill.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"
})
public class SeckillServiceImplTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {
        List<Seckill> list = seckillService.getSeckillList();
        logger.info("list={}",list);
    }

    @Test
    public void getById() {
        long id = 1000;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill={}",seckill);
    }

    @Test
    public void exportSeckillUrl() {
        long id =1004;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        logger.info("exposer={}",exposer);
        /**
         * exposed=true,
         * md5='a5b26308fe2f1506c3f62a11bc98769e'
         * ada0dde4d28b4f23c0546e9aa616c5ad,
         * seckillId=1004,
         * now=0,
         * start=0,
         * end=0
         */
    }

    @Test
    public void executeSeckill() {
        long id = 1004;
        long phone = 1350217113L;
        String md5="a5b26308fe2f1506c3f62a11bc98769e";
        SeckillExecution execution = seckillService.executeSeckill(id,phone,md5);
        logger.info("result={}",execution);
    }
}