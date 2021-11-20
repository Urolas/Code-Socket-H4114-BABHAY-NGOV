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

            try (FileWriter fw = new FileWriter("logFiles/allUsers.txt",true)) {
                fw.write(username+"\n"); //appends the new username to the file
                fw.close();

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

        try (FileWriter fw = new FileWriter("logFiles/Log_"+username+".txt",true)) {
            fw.write(message+"\n"); //appends the new username to the file
            fw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    //read the log file
    public static String getHistory(String username){
        String log="";

        try(FileReader fileReader = new FileReader("logFiles/Log_"+username+".txt")) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log += line+"\n";
            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }

        return log;
    }


}
