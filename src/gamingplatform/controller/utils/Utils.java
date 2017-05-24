package gamingplatform.controller.utils;

import gamingplatform.dao.exception.DaoException;
import gamingplatform.dao.implementation.LevelDaoImpl;
import gamingplatform.dao.implementation.UserDaoImpl;
import gamingplatform.dao.interfaces.LevelDao;
import gamingplatform.dao.interfaces.UserDao;
import gamingplatform.model.Level;
import gamingplatform.model.User;
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

        //TODO fare questo controllo lato servlet se no rimette sempre l'immagine di default a ogni update
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

    /**
     * ritorna l'ultimo segmento della url passata
     * @param url url da analizzare
     * @return ultimo segmento
     */
    public static String getLastBitFromUrl(final String url){
        return url.replaceFirst(".*/([^/?]+).*", "$1");
    }


    /**
     * ritorna l'n-ultimo segmento della url passata
     * es. /terzultimo/penultimo/ultimo con n=1 torna "penultimo"
     * @param url url da analizzare
     * @param n indica l'elemento da tornare
     * @return n-ultimo segmento
     */
    public static String getNlastBitFromUrl(final String url, int n){

        String segment="";
        int length=0;

        for(int i=0; i<=n; i++){
            //System.out.println("passata "+i+", segment: "+segment+", length: "+length);
            segment=getLastBitFromUrl(url.substring(0,url.length()-length));
            length+=segment.length()+1; //+1 per il "/"
        }

        //System.out.println("return: "+segment);
        return segment;
    }


    public static int checkLevel( User user ){

        LevelDao ld = new LevelDaoImpl(ds);
        UserDao ud = new UserDaoImpl(ds);

        try {


            //dati attuali
            int esperienzaPrecedente = (ud.getLevelByUserId(user.getId())).getExp(); // livello a cui si trovava prima l'utente

            int esperienzaAttuale = user.getExp(); // esperienza a cui si trova attualmente l'utente

            List<Level> levelList = ld.getLevels(); // lista contenente TUTTI i livelli


            /*

             */


            int result = 0; //vaore che varra' restituito


            //l'utente ha meno esperienza di prima, ha perso livelli
            if( esperienzaAttuale < esperienzaPrecedente ){


                for( Level l : levelList ){ // ciclo sulla lista dei livelli


                    if( esperienzaAttuale < l.getExp() && esperienzaPrecedente >= l.getExp()  ){
                        /* se l'esperieza del livello e' maggiore o uguale a quella precedente dell'utente e
                           minore o uguale a quella dell'esperienza successiva
                        */

                       result = result - 1;

                    }
                }
            }else {


                //l'utente ha piu' esperienza di prima
                if (esperienzaAttuale > esperienzaPrecedente) {


                    for (Level l : levelList) { // ciclo sulla lista dei livelli

                        if (esperienzaAttuale >= l.getExp() && esperienzaPrecedente < l.getExp()) {
                        /* se l'esperieza del livello e' maggiore o uguale a quella precedente dell'utente e
                           minore o uguale a quella dell'esperienza successiva
                        */

                            result = result + 1;


                        }
                    }
                }else{
                    return 0;
                }
            }

            } catch (DaoException e) {
            e.printStackTrace();
        }


        return 0;
    }




}
