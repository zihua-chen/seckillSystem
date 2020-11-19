package org.seckill.dao;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.SuccessKilled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKillDaoTest {
    @Resource
    private SuccessKillDao successKillDao;
    @Test
    public void insertSuccessKilled() {
        long id = 1001L;
        long phone = 13970713285l;
        int insertCount = successKillDao.insertSuccessKilled(id, phone);
        System.out.println("insertcount:"+insertCount);

    }

    @Test
    public void queryByIdWithSeckill() {
        long id = 1001l;
        long phone = 13970713285l;
        SuccessKilled successKilled = successKillDao.queryByIdWithSeckill(id,phone);
        System.out.println(successKilled);
        System.out.println(successKilled.getSeckill());
    }
}
