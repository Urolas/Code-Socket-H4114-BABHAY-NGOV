package stream;

import java.io.*;
import java.nio.file.Files;

public class LogManager {

    public static void findNameInFile(String username){

        boolean found = false;

        //Find the name of the user in the user list
        try(FileReader fileReader = new FileReader("logFiles/allUsers.txt")) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if(line.equals(username)){
                    found = true;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }

        //if the user doesn't exist, create a Log file for them
        if(!found) {

            try (PrintWriter out = new PrintWriter("logFiles/allUsers.txt")) {
                out.println(username);

                //and create the file
                File newFile = new File("logFiles/Log_"+username+".txt");
                newFile.createNewFile();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println(e);
            }
        }

    }

    //write a new line on the log txt file
    public static void writeOnUserLog(String username, String message){

        try (PrintWriter out = new PrintWriter("logFiles/Log_"+username+".txt")) {
            out.println(message);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e);
        }
    }


}
