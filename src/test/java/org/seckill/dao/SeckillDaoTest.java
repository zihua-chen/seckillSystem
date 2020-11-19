package org.seckill.dao;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


/**
 * 配置spring和junit的整合，junit启动时加载springIOC容器
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉JunitSpring配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest extends TestCase {
    //注入Dao类依赖
    @Resource
    private SeckillDao seckillDao;
    @Test
    public void testQueryById() throws Exception{
        long id=1000;
        Seckill seckill =seckillDao.queryById(id);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }
    @Test
    public void testQueryAll() {
        //使用Param去绑定参数
        //java在运行时不会去保存形参，而是把参数变成了arg0，arg1的形式
        List<Seckill> seckills=seckillDao.queryAll(0,100);
        for( Seckill seck:seckills){
            System.out.println(seck);
        }

    }
    @Test
    public void testReduceNumber() {
        Date killTime = new Date();
        int updateCount = seckillDao.reduceNumber(1000L,killTime);
        System.out.println("updatecount:"+updateCount);
    }
}