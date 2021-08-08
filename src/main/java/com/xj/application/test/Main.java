//package com.xj.application.test;
//
//import com.xj.application.annotation.Component;
//import com.xj.application.annotation.Inject;
//import com.xj.application.core.ApplicationContext;
//import com.xj.application.persistence.core.Executor;
//import com.xj.application.service.OrderDao;
//import com.xj.application.service.OrderService;
//import com.xj.application.service.impl.MobileOrderServiceImpl;
//import com.xj.application.service.impl.OrderServiceImpl;
//
//import java.io.*;
//
//@Component
//public class Main {
//
//    @Inject(type = OrderServiceImpl.class)
//    private OrderService orderService;
//
//    @Inject(type = MobileOrderServiceImpl.class)
//    private OrderService mobileOrderServiceImpl;
//
//    @Inject
//    private OrderDao orderDao;
//
//    @Inject(name = "createExecutor")
//    private Executor executor;
//
//
//
//
//
//
//    public static void main(String[] args) throws Exception {
//        ApplicationContext applicationContext = ApplicationContext.getApplicationContext("com.test");
//
//        Main main = applicationContext.getBean(Main.class);
//
//        System.out.println(main.orderService);
//
//        System.out.println(main.mobileOrderServiceImpl);
//
//
////        List<Student> list = main.executor.query("select s.*,c.*,p.* from student s left join course c on s.id = c.sid left join payment p on c.id = p.cid limit ?,?", Student.class, 0, 9);
////        list.forEach(System.out::println);
//
////        URL url1 = Main.class.getClassLoader().getResource("com/xj");
////        System.out.println(url1);
//////        System.out.println(url1);
////
////        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("com/mysql");
////
////        while (resources.hasMoreElements()) {
////            URL url = resources.nextElement();
////            System.out.println(url);
////            System.out.println(url.getProtocol());
////            if (url.getProtocol().equals("jar")) {
////                JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
////                JarFile jarFile = urlConnection.getJarFile();
////                Enumeration<JarEntry> entries = jarFile.entries();
////                while (entries.hasMoreElements()){
////                    JarEntry jarEntry = entries.nextElement();
////                    String name = jarEntry.getName();
////                    if (name.endsWith(".class")){
////                        System.out.println(name);
////                    }
////                }
////            }
////        }
//
//
////        Main main = new Main();
////
////        File targetFile = new File("result.dat");
////        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile)));
////        main.pickUp(new File("F:/workspace/PersonalIOC/src/main/java"),bufferedWriter);
//    }
//
//    public void pickUp(File file,BufferedWriter bufferedWriter){
//        File[] files = file.listFiles();
//        for (File file1 : files) {
//            if (file1.isDirectory()){
//                pickUp(file1,bufferedWriter);
//            }
//            if (file1.isFile()) {
//                if (file1.getName().endsWith(".java") && !file1.getName().contains("Main")){
//                    write(file1,bufferedWriter);
//                }
//            }
//        }
//    }
//
//    public  void write(File file,BufferedWriter bufferedWriter){
//        try {
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
//            int right = 0;
//            String msg =  null;
//            boolean flag = false;
//            int lineNum = 1;
//            int isHead = 0;
//
//            while ((msg = bufferedReader.readLine()) != null) {
//
//                if (!msg.trim().startsWith("//") && msg.contains("catch")) {
//                    bufferedWriter.write(file.getAbsolutePath());
//                    bufferedWriter.newLine();
//                    bufferedWriter.flush();
//                    flag = true;
//                    isHead++;
//                }
//                //catch 语句
//                if (flag) {
//                    if (msg.contains("{") ) {
//                        right ++;
//                    }
//                    //第一个catch语句时
//                    if (isHead == 1) {
//                        if (msg.contains("}") && !msg.contains("catch")) {
//                            right --;
//                        }
////                        if (msg.contains("finally")){
////                            right = 0;
////                        }
//
//
//                        //多个catch语句时
//                    } else {
//                        if (msg.contains("}")) {
//                            right --;
//                        }
//                        //catch语句数减少
////                        if (msg.contains("finally")){
////                            isHead --;
////                        }
//                    }
//                    if (right == 0 && !msg.contains("Exception")) {
//                        flag =false;
//                        isHead = 0;
//                    }
//                    bufferedWriter.write(" lineNum: " + lineNum + "  " +msg);
//                    bufferedWriter.newLine();
//                    bufferedWriter.flush();
//                }
//                lineNum++;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
//
