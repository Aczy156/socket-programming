import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class MyClient implements ActionListener{
	Socket s;
	DataInputStream input;
	DataOutputStream output;

	JButton sendButton, logoutButton,loginButton, exitButton;
	JFrame chatWindow;
	JTextArea txtBroadcast;
	JTextArea txtMessage;
	JList usersList;

	public MyClient(){
		displayGUI();
	}
    public void displayGUI() {
        chatWindow = new JFrame();
        chatWindow.setPreferredSize(new Dimension(550, 700));
        // 消息台展示历史消息
        txtBroadcast = new JTextArea(20, 30);
        txtBroadcast.setEditable(false);
        // 输入窗口
        txtMessage = new JTextArea(5, 20);
        usersList = new JList();
        // 当前在线用户展示
        usersList.setVisibleRowCount(10);

        sendButton = new JButton("发送");
        logoutButton = new JButton("下线");
        loginButton = new JButton("登录");
        exitButton = new JButton("退出");

        JPanel center1 = new JPanel();
        center1.setLayout(new BorderLayout());
        // 容器添加组建
        center1.add(new JLabel("控制台：历史消息", JLabel.CENTER), "North");
        center1.add(new JScrollPane(txtBroadcast), "Center");

        JPanel bottom1 = new JPanel();
        bottom1.setLayout(new FlowLayout());
        bottom1.add(new JScrollPane(txtMessage));
        bottom1.add(sendButton);

        JPanel bottom2 = new JPanel();
        bottom2.setLayout(new FlowLayout());
        bottom2.add(loginButton);
        bottom2.add(logoutButton);
        bottom2.add(exitButton);

        JPanel bottom = new JPanel();
        bottom.setLayout(new GridLayout(2, 2));
        bottom.add(bottom1);
        bottom.add(bottom2);

        JPanel right = new JPanel();
        right.setLayout(new BorderLayout());
        right.add(new JLabel("当前在线用户", JLabel.CENTER), "North");
        right.add(new JScrollPane(usersList), "Center");

        // frame 添加组件
        chatWindow.add(center1, "Center");
        chatWindow.add(right, "East");
        chatWindow.add(bottom, "South");
        chatWindow.pack();
        chatWindow.setTitle("请先登录，然后进行聊天");
        chatWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        chatWindow.setVisible(true);

        sendButton.addActionListener(this);
        logoutButton.addActionListener(this);
        loginButton.addActionListener(this);
        exitButton.addActionListener(this);
        // 按钮的enable初始化设置
        logoutButton.setEnabled(false);
        loginButton.setEnabled(true);

        txtMessage.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent fe) {
                txtMessage.selectAll();
            }
        });

        chatWindow.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                if (s != null) {
                    JOptionPane.showMessageDialog(chatWindow, "当前处于离线状态 ", "Exit", JOptionPane.INFORMATION_MESSAGE);
                    logoutSession();
                }
                System.exit(0);
            }
        });
    }

	public void actionPerformed(ActionEvent ae){
		JButton tmp=(JButton)ae.getSource();
		if(tmp == sendButton){
			if(s==null){
				JOptionPane.showMessageDialog(chatWindow,"请先登陆，然后开始使用");
				return;
			}
			try{
				output.writeUTF(txtMessage.getText());
				txtMessage.setText("");
			}
			catch(Exception excp){
				txtBroadcast.append("send button click :"+excp);
			}
		}
		else if(tmp == loginButton){
			String userName=JOptionPane.showInputDialog(chatWindow,"请输入你的昵称");
			if(userName!=null)
				clientChat(userName);
		}
		else if(tmp == logoutButton)
			if(s != null)
				logoutSession();
		else if(tmp == exitButton){
			if(s != null){
				JOptionPane.showMessageDialog(chatWindow,"当前已处于离线状态","Exit",JOptionPane.INFORMATION_MESSAGE);
				logoutSession();
			}
			System.exit(0);
		}
	}

	public void logoutSession()
	{
		if(s==null) return;
		try{
			output.writeUTF(MyServer.LOGOUT_MESSAGE);
			Thread.sleep(500);
			s=null;
		}
		catch(Exception e){txtBroadcast.append("\n inside logoutSession Method"+e);}

		logoutButton.setEnabled(false);
		loginButton.setEnabled(true);
		chatWindow.setTitle("登录");
	}

	public void clientChat(String userName){
		try{
			// 服务器的端口
			s=new Socket(InetAddress.getLocalHost(),MyServer.PORT);
			input = new DataInputStream(s.getInputStream());
			output = new DataOutputStream(s.getOutputStream());
			ClientThread ct=new ClientThread(input,this);
			Thread t1=new Thread(ct);
			t1.start();
			output.writeUTF(userName);
			chatWindow.setTitle(userName + "的聊天窗口");
			logoutButton.setEnabled(true);
			loginButton.setEnabled(false);
		}
		catch(Exception e){
			txtBroadcast.append("\nClient Constructor " +e);
		}

	}
	public static void main(String argus[]){
		new MyClient();
	}
}

class ClientThread implements Runnable{
	DataInputStream input;
	MyClient client;
	public ClientThread(DataInputStream input, MyClient client){
		this.input = input;
		this.client = client;
	}
	public void run(){
		String s2 = "";
		try{
			while(true){
				s2 = input.readUTF();
				System.out.println(s2);
				if(s2.startsWith(MyServer.UPDATE_USERS)){
					updateUsersList(s2);
				}
				else if(s2.startsWith(MyServer.LOGOUT_MESSAGE)){
					// 用户下线，清除该用户的消息列表
					client.usersList.setListData(new Vector());
					break;
				}
				else
					client.txtBroadcast.append("\n"+s2);
				int lineOffset=client.txtBroadcast.getLineStartOffset(client.txtBroadcast.getLineCount()-1);
				client.txtBroadcast.setCaretPosition(lineOffset);
			}
		}
		catch(Exception e){
			System.err.println("ClientThread Run " + e);
		}
	}
	public void updateUsersList(String ul){
		Vector ulist=new Vector();
		ul=ul.replace("[","");
		ul=ul.replace("]","");
		ul=ul.replace(MyServer.UPDATE_USERS,"");
		StringTokenizer st=new StringTokenizer(ul,",");
		while(st.hasMoreTokens())
		{
			String temp=st.nextToken();
			ulist.add(temp);
		}
		client.usersList.setListData(ulist);
	}
}