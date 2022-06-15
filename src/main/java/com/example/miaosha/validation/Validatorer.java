package com.example.miaosha.validation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

@Component
public class Validatorer implements InitializingBean {
    Validator validator;

    // 进行校验
    public ValidationResult validate(Object bean){
        ValidationResult result=new ValidationResult();

        Set<ConstraintViolation<Object>> errSets=validator.validate(bean);
        if(errSets.size()>0){
            result.setHasError(true);
            errSets.forEach(err->{
                String errMsg=err.getMessage();
                String name=err.getPropertyPath().toString();

                result.getErrMaps().put(name,errMsg);
            });
        }
        return result;
    }


    // 实例化校验器
    @Override
    public void afterPropertiesSet() throws Exception {
        this.validator= Validation.buildDefaultValidatorFactory().getValidator();
    }
}
