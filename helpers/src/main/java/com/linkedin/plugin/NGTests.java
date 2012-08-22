// Copyright 2012 LinkedIn
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//    http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.linkedin.plugin;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.File;

import org.testng.*;
import play.test.*;
import static play.test.Helpers.*;

public class NGTests implements IHookable{
  
  private Method testMethod(ITestResult testResult){
    return testResult.getMethod().getConstructorOrMethod().getMethod();
  }
  
  private Class testClass(ITestResult testResult){
    return testResult.getTestClass().getRealClass();
  }
  
  private WithFakeApplication getFakeApp(ITestResult testResult){
    Class clazz = testClass(testResult);
    Method m = testMethod(testResult);
    
    WithFakeApplication classFakeApp = (WithFakeApplication)clazz.getAnnotation(WithFakeApplication.class);
    WithFakeApplication a = m.getAnnotation(WithFakeApplication.class);

    if(a != null)
      return a;
    else
      return classFakeApp;
  }
  
  private Map<String, String> getConf(ITestResult testResult){
    Map<String, String> conf = new HashMap<String, String>();
    
    if(getFakeApp(testResult) == null)
      return conf;
    
    Class clazz = testClass(testResult);
    Method m = testMethod(testResult);
    
    Confs classConfs = (Confs)clazz.getAnnotation(Confs.class);
    Conf classConf = (Conf)clazz.getAnnotation(Conf.class);
    Confs methodConfs = m.getAnnotation(Confs.class);
    Conf methodConf = m.getAnnotation(Conf.class);
    
    if(classConfs != null){
      for(Conf c : classConfs.value())
        conf.put(c.key(), c.value());
    }
    
    if(classConf != null)
      conf.put(classConf.key(), classConf.value());
    
    if(methodConfs != null){
      for(Conf c : methodConfs.value())
        conf.put(c.key(), c.value());
    }
    
    if(methodConf != null)
      conf.put(methodConf.key(), methodConf.value());
      
    return conf;
  }
  
  private List<String> getPlugins(ITestResult testResult){
    Class clazz = testClass(testResult);
    Method m = testMethod(testResult);

    WithPlugins classPlugins = (WithPlugins)clazz.getAnnotation(WithPlugins.class);
    WithPlugins methodPlugins = m.getAnnotation(WithPlugins.class);

    List<String> plugins = new ArrayList<String>();
    if(classPlugins != null)
      plugins.addAll(Arrays.asList(classPlugins.value()));
    if(methodPlugins != null)
    plugins.addAll(Arrays.asList(methodPlugins.value()));

    return plugins;
  }

  public void run(final IHookCallBack icb, ITestResult testResult) {
    WithFakeApplication fa = getFakeApp(testResult);
    if(fa != null){
      String path = fa.path();
      FakeApplication app = new FakeApplication(new File(path), Helpers.class.getClassLoader(), getConf(testResult), getPlugins(testResult));
      start(app);
      icb.runTestMethod(testResult);
      stop(app);
    }
    else{
      icb.runTestMethod(testResult);
    }
   }
}
