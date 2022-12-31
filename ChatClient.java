import java.io.*;
import java.net.Socket;

public class ChatClient implements Runnable{
private Socket client;
private BufferedReader in;
private PrintWriter out;
private boolean done;

    @Override
    public void run() {
        try {
            client=new Socket("localhost",3000);
            out=new PrintWriter(client.getOutputStream(),true);
            in=new BufferedReader(new InputStreamReader(client.getInputStream()));
      InputHandler inputHandler=new InputHandler();
      Thread t=new Thread(inputHandler);
      t.start();
      String inMessage;
      while ((inMessage=in.readLine())!=null){
          System.out.println(inMessage);
      }
        }catch (IOException e){
            shutDown();
        }
    }
    public  void shutDown(){
        done=true;
        try {
            in.close();
            out.close();
            if (!client.isClosed())
                client.close();
        }catch (Exception e){
            e.getMessage();
        }
    }
    class InputHandler implements Runnable{

        @Override
        public void run() {
            try {
                BufferedReader inputReader=new BufferedReader(new InputStreamReader(System.in));
                while (!done){
                    String message=inputReader.readLine();
                    if (message.equals("LOGOUT")){
                        out.println(message);
                        inputReader.close();
                        shutDown();
                    }else {
                        out.println(message);
                    }
                }
            }catch (IOException e){
                shutDown();
            }

        }
    }

    public static void main(String[] args) {
        ChatClient client=new ChatClient();
        client.run();
    }
}
