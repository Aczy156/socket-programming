import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;


class MyServer {
    ArrayList sockets = new ArrayList();
    ArrayList users = new ArrayList();
    ServerSocket ss;
    Socket s;

    public final static int PORT = 10000;
    public final static String UPDATE_USERS = "当前用户列表为:";
    public final static String LOGOUT_MESSAGE = "下线:";

    public MyServer() {
        try {
            // 设置异常抛出
            ss = new ServerSocket(PORT);
            System.out.println("服务器开始运行 " + ss);
            while (true) {
                // 阻塞，直到建立连接为止
                s = ss.accept();
                Runnable r = new MyThread(s, sockets, users);
                Thread thread = new Thread(r);
                thread.start();
            }

        } catch (Exception e) {
            System.err.println("Server constructor " + e);
        }
    }

    public static void main(String argus[]) {
        new MyServer();
    }
}

class MyThread implements Runnable {
    ArrayList sockets = new ArrayList();
    ArrayList users = new ArrayList();
    SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd :hh:mm:ss");
    Socket s;
    String userName;

    public MyThread(Socket s, ArrayList sockets, ArrayList users) {
        this.s = s;
        this.users = users;
        this.sockets = sockets;
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd :hh:mm:ss");
        try {
            DataInputStream input = new DataInputStream(s.getInputStream());
            sockets.add(s);
            userName = input.readUTF();
            users.add(userName);
            broadCast(userName + "已登录，时间为" + dateFormat.format(new Date()));
            sendNewUserList();
        } catch (Exception e) {
            System.err.println("MyThread constructor " + e);
        }
    }

    public void run() {
        String s1;
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd :hh:mm:ss");
        try {
            DataInputStream input = new DataInputStream(s.getInputStream());
            while (true) {
                s1 = input.readUTF();
                if (s1.toLowerCase().equals(MyServer.LOGOUT_MESSAGE)) {
                    break;
                }
                broadCast(userName + "在" + dateFormat.format(new Date()) + "时间发布消息：" + s1);
            }

            DataOutputStream output = new DataOutputStream(s.getOutputStream());
            output.writeUTF(MyServer.LOGOUT_MESSAGE);
            output.flush();

            users.remove(userName);
            broadCast(userName + "已下线，时间为" + dateFormat.format(new Date()));
            sendNewUserList();
            sockets.remove(s);
            s.close();

        } catch (Exception e) {
            System.err.println("MyThread run " + e);
        }
    }

    public void broadCast(String str) {
        Iterator iter = sockets.iterator();
        while (iter.hasNext()) {
            try {
                Socket broadSoc = (Socket) iter.next();
                DataOutputStream output = new DataOutputStream(broadSoc.getOutputStream());
                output.writeUTF(str);
                output.flush();
            } catch (Exception e) {
                System.err.println("MyThread broadCast " + e);
            }
        }
    }

    public void sendNewUserList() {
        broadCast(MyServer.UPDATE_USERS + users.toString());
    }
}