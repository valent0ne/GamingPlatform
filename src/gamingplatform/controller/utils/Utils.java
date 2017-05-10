package gamingplatform.controller.utils;

import org.apache.commons.io.FileUtils;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.Part;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static gamingplatform.controller.utils.SecurityLayer.addSlashes;
import static java.util.Objects.isNull;

public class Utils {

    @Resource(name = "jdbc/gamingplatform")
    private static DataSource ds;

    /**
     * dato un filePart (file proveniente da una form), controlla se il file è un .jpg
     * se si lo copia nella directory specificata e torna il nome del file salvato
     * @param filePart oggetto Part contenente dati del file
     * @param directory directory in cui verrà salvto il file
     * @return il nome del file salvato, oppure null su errore
     */
    public static String fileUpload(Part filePart, String directory, ServletContext svc){

        String fileName = addSlashes(Paths.get(filePart.getSubmittedFileName()).getFileName().toString());

        if(isNull(fileName) || fileName.equals("")){
            return "default.png";
        }

        //se non è un .jpg oppure un .png
        if(!(fileName.substring(fileName.length()-4).equals(".jpg") ||
             fileName.substring(fileName.length()-4).equals(".png"))){

            Logger.getAnonymousLogger().log(Level.WARNING,"[Utils] fileUpload fallito, formato non corretto: "+fileName+" - formato: "+fileName.substring(fileName.length()-4));
            return null;
        }

        try {
            //aggiungo il tempo in millisecondi prima del nome (così mantengo il .jpg) per evitare conflitti
            fileName = System.currentTimeMillis()+fileName;
            InputStream fileContent = filePart.getInputStream();
            //salvo il file nella directory specificata
            File targetFile = new File(svc.getRealPath("template/"+directory+"/"+fileName));
            FileUtils.copyInputStreamToFile(fileContent, targetFile);
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.WARNING,"[Utils] fileUpload IOException "+e.getMessage());
            return null;
        }

        return fileName;
    }


    public static List<String> getClassFields(Object o) {
        Class<?> clazz = o.getClass();
        List<String> list= new ArrayList<>();

        for(Field field : clazz.getDeclaredFields()) {
            if(field.getName().equals("password")){
                continue;
            }
            list.add(field.getName());
        }
        return list;
    }



}