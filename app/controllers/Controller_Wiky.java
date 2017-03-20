package controllers;


import io.swagger.annotations.Api;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Wiky extends Controller {

     public Result test1(){

         try {

             List<Long> numbers = new ArrayList<>();

             for (int i = 0; i < 10; i++){
                 numbers.add(new Long(i));
             }

             numbers.forEach(number->{
                 if (number % 2 == 0){
                     System.out.println(number);
                 }
             });

             numbers.forEach(System.out::println);

             return ok();
         }catch (Exception e){
             e.printStackTrace();
             return badRequest();
         }

     }





}
