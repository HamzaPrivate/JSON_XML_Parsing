import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.json.JSONArray;
import org.json.JSONObject;

/*
 * Aufgabe:
 * Informationen aus der Datei vom Chef zu verarbeiten, damit Sie die Anzahl von
 * erreichteten Noten im Fach “Programmierung I” zählen, z.B. 10 Studierende haben die
 * Note 1,0 erreicht, 20 haben 1,3, 30 haben 1,7, usw
 */

public class Reader {

	public static void main(String[] args) {
		Map<String, Integer> progNoten = new HashMap<>();
		//XML
		Document document = null;
		try {
			SAXReader reader = new SAXReader();
			reader.setEncoding("UTF-8");
			File file = new File("input/studenten.xml");
			document = reader.read(file);
			Element studenten = document.getRootElement();
			// Mehrere Elemente über eine List zugreifen
			List<Element> studentElemente = studenten.elements();
			for (Element e: studentElemente) {
				Element faecher = e.element("Faecher");
				List<Element> faecherList = faecher.elements();
				for(Element e2 : faecherList) {
					Element fach = e2.element("Name");
					if(fach.getText().equals("Programmierung I")) {
						Element note = e2.element("Note");
						String noteAlsText = note.getText();
						if(progNoten.containsKey(noteAlsText)) {
							int anzahl = progNoten.get(noteAlsText);
							progNoten.put(noteAlsText, ++anzahl);
						}
						else {
							progNoten.put(noteAlsText, 1);
						}
					}
				}
			}
		}
		catch (DocumentException e) {
			e.printStackTrace();
		}

		Document docToSave = DocumentHelper.createDocument();
		Element faecherElement = docToSave.addElement("Faecher");
		Element fachElement = faecherElement.addElement("Fach")
				.addAttribute("Name", "Programmierung I");
		var toSort = new ArrayList<String>(progNoten.keySet());

		Collections.sort(toSort);
		for(String s : toSort) {
			Element statElement = fachElement.addElement("Statistik");
			statElement.addElement("Note").addText(s);
			statElement.addElement("Anzahl").addText("" + progNoten.get(s));
		}
	
		try {
			FileOutputStream fos = new FileOutputStream("output/results.xml");
			OutputFormat format = OutputFormat.createPrettyPrint();
			// Schoen formatiert
			// Alternativ:
			// OutputFormat format = OutputFormat.createCompactFormat();
			// Keine Baum-Struktur
			format.setEncoding("UTF-8");
			XMLWriter writer = new XMLWriter(fos, format);
			writer.write(docToSave);
			fos.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		
		
		//Json
		Map<String, String> progNotenJSON = new HashMap<>();
		//Haupt-JSON-Object aus einer Datei einlesen
		JSONObject hauptJSONObject = null;
		// Erst "ganz normal" als Text-Datei mit java.io einlesen,
		// dann der String zum Haupt-JSON-Object umwandeln
		try {
			FileInputStream fis = new FileInputStream("input/studenten.json");
			InputStreamReader isr = new InputStreamReader(fis, "UTF8");
			BufferedReader br = new BufferedReader(isr);
			String gesamterInhalt = "";
			String line = "";
			while ((line = br.readLine()) != null){
				gesamterInhalt += line;
			}
			hauptJSONObject = new JSONObject(gesamterInhalt);

			JSONArray studentenJSONArray = hauptJSONObject.getJSONArray("Studenten");
			for(int i = 0; i<studentenJSONArray.length();i++) {
				JSONObject obj = studentenJSONArray.getJSONObject(i);
				String note =(String) obj.getJSONObject("Faecher").get("Programmierung I");
				if(progNotenJSON.containsKey(note)) {
					int anzahl = Integer.parseInt(progNotenJSON.get(note))+1;
					progNotenJSON.put(note, "" +anzahl);
				}
				else {
					progNotenJSON.put(note, "1");
				}
			}
			// Aufraeumen
			br.close();
			isr.close();
			fis.close();

		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		
		JSONObject hauptJSONObjectSave = new JSONObject();
		JSONArray JSONArray = new JSONArray();
		JSONObject objectInArray = new JSONObject();
		JSONArray.put(objectInArray);
		objectInArray.put("Name", "Programmierung I");
		objectInArray.put("Statistiken", progNotenJSON);
		hauptJSONObjectSave.put("Faecher", JSONArray); 
		try {
		FileOutputStream fos = new FileOutputStream("output/results.json");
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(hauptJSONObjectSave.toString(2)); // Schoen formatiert
		// Alternativ:
		// bw.write(hauptJSONObject.toString()); // Keine Baum-Struktur
		bw.newLine();
		// Aufraeumen
		bw.close();
		osw.close();
		fos.close();
		}
		catch (IOException e) {
		e.printStackTrace();
		}
	}
}



