package org.xiatian.shortlink.project.common.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.xiatian.shortlink.project.common.annotation.ClearAndReloadCache;
import org.xiatian.shortlink.project.common.convention.exception.ServiceException;
import org.xiatian.shortlink.project.dto.req.recyclebin.RecycleBinSaveReqDTO;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ClearAndReloadCacheAspect {

    private final StringRedisTemplate stringRedisTemplate;

    /**
    * 切入点
    *切入点,基于注解实现的切入点  加上该注解的都是Aop切面的切入点
    */

    @Pointcut("@annotation(org.xiatian.shortlink.project.common.annotation.ClearAndReloadCache)")
    public void pointCut(){
    }

    /**
    * 环绕通知
    * 环绕通知非常强大，可以决定目标方法是否执行，什么时候执行，执行时是否需要替换方法参数，执行完毕是否需要替换返回值。
    * 环绕通知第一个参数必须是org.aspectj.lang.ProceedingJoinPoint类型
     */
    @Around("pointCut()")
    public Object aroundAdvice(ProceedingJoinPoint proceedingJoinPoint){
        System.out.println("环绕通知的目标方法名：" + proceedingJoinPoint.getSignature().getName());
        Signature signature1 = proceedingJoinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature)signature1;
        Method targetMethod = methodSignature.getMethod();//方法对象
        ClearAndReloadCache annotation = targetMethod.getAnnotation(ClearAndReloadCache.class);//反射得到自定义注解的方法对象

        String name = annotation.name();//获取自定义注解的方法对象的参数即name
        Object[] params = proceedingJoinPoint.getArgs();
        if(params == null) throw new ServiceException("删除缓存失败");
        RecycleBinSaveReqDTO requestParam = (RecycleBinSaveReqDTO)params[0];
        String deleteKey = name+requestParam.getFullShortUrl();
        //第一次删除缓存
        stringRedisTemplate.delete(deleteKey);

        //执行加入双删注解的改动数据库的业务 即controller中的方法业务
        Object proceed = null;
        try {
            proceed = proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        // 开一个线程 延迟2秒
        // 在线程中延迟删除  同时将业务代码的结果返回 这样不影响业务代码的执行
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                //第二次删除缓存
                stringRedisTemplate.delete(deleteKey);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        //返回业务执行结果
        return proceed;
    }
}