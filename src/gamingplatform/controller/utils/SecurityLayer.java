package gamingplatform.controller.utils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;

import gamingplatform.view.FreemarkerHelper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SecurityLayer {


    public static boolean checkAuth(){
        //TODO
        return true;
    }

    /**
     * codifica la stringa in sha1
     * @param x stringa da codificare
     * @return sha1(x) oppure null
     */
    public static String sha1Encrypt(String x) {
        String sha1 = null;
        try {
            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
            msdDigest.update(x.getBytes("UTF-8"), 0, x.length());
            sha1 = DatatypeConverter.printHexBinary(msdDigest.digest());
            sha1=sha1.toLowerCase();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            Logger.getAnonymousLogger().log(Level.WARNING, "SecurityException: " + e.getMessage());
        }
        return sha1;
    }


    /**
     * aggiorna i valori di gruppi/servizi a cui appartiene l'utente e li carica in sessione
     *
     * @param request richiesta servlet
     */

    public static void updateAuth(HttpServletRequest request, HttpServletResponse response) {

        HttpSession session = SessionManager.verifySession(request);
        //TODO update permessi
    }


    /**
     * abort della servlet con messaggio di errore rimanendo sulla stessa pagina
     * @param tpl template da elaborare da freemarker
     * @param data map con i dati da elaborare da freemarker
     * @param message messaggio da notificare all'user
     * @param response risposta servlet
     * @param svc servlet context
     */
    public static void abort(String tpl, Map<String, Object> data, String message, HttpServletResponse response, ServletContext svc){

        data.put("message", message);
        FreemarkerHelper.process(tpl, data, response, svc);

    }

    /**
     * abort della servlet con messaggio e redirect
     * @param redirect pagina a cui effettuare il redirect
     * @param message messaggio da notificare all'user
     * @param response risposta servlet
     * @param request richiesta servlet
     */
    public static void redirect(String redirect, String message, HttpServletResponse response, HttpServletRequest request){
        SessionManager.pushMessage(request, message);
        try {
            response.sendRedirect(redirect);
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.WARNING, "Redirect fallito "+e.getMessage());
        }

    }



    //--------- DATA SECURITY ------------
    //

    /**
     * questa funzione aggiunge un backslash davanti a
     * tutti i caratteri "pericolosi", usati per eseguire
     * SQL injection attraverso i parametri delle form
     * @param s stringa da modificare
     * @return stringa pulita
     */
    public static String addSlashes(String s) {
        return s.replaceAll("(['\"\\\\])", "\\\\$1");
    }

    /**
     * questa funzione rimuove gli slash aggiunti da addSlashes
     * @param s stringa da modificare
     * @return stringa pulita
     */
    public static String stripSlashes(String s) {
        return s.replaceAll("\\\\(['\"\\\\])", "$1");
    }

    /**
     * converte la stringa in numero
     * @param s stringa da onvertire
     * @return intero corrispondente
     * @throws NumberFormatException in caso di errore
     */
    public static int checkNumeric(String s) throws NumberFormatException {
        //convertiamo la stringa in numero, ma assicuriamoci prima che sia valida
        //convert the string to a number, ensuring its validity
        if (s != null) {
            //se la conversione fallisce, viene generata un'eccezione
            //if the conversion fails, an exception is raised
            return Integer.parseInt(s);
        } else {
            throw new NumberFormatException("String argument is null");
        }
    }
}
