import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.text.html.parser.AttributeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

/**
 * Created by Oleg Pavlin on 17.06.14.
 */
public class Backup extends JFrame {
	private JButton submit;
	private JPanel rootPanel;
	private JTextArea results;
	public JProgressBar downloaded;
	private JComboBox sitesBox;

	public ArrayList<Sites> sites;

	public Backup() {
		super("Hello world");
	}

	private void createUIComponents() {
		// TODO: place custom component creation code here
	}

	public class ProcessDownload extends SwingWorker<Integer, Integer> {

		public URLConnectionReader filesList;
		public URLConnectionReader runBackup;

		public Integer doInBackground() throws Exception {
			Integer siteIndex = sitesBox.getSelectedIndex();
			String fileListUrl = sites.get(siteIndex).fileListUrl;
			String sxdUrl = sites.get(siteIndex).sxdUrl;
			String backupLocation = sites.get(siteIndex).backupLocation;

			results.append("Making backup of " + sitesBox.getSelectedItem() + "\n");

			try {
				runBackup = new URLConnectionReader(sxdUrl);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			try {
				results.append("Waiting 20 seconds before downloading backups\n");
				Thread.sleep(20000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			try {
				filesList = new URLConnectionReader(fileListUrl);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			int counter = 1;
			int userFileCount = filesList.files.size();

			results.append("Found " + userFileCount + " files" + "\n");

			downloaded.setMaximum(userFileCount);
			downloaded.setMinimum(0);


			for (Object filename : filesList.files) {
				downloaded.setString("Downloading file #" + counter + " of " + userFileCount);

				File f = new File((String) filename);
				if (f.exists() && !f.isDirectory()) { // skip downloading existing files, but do progress bar ++
					results.append("File #" + counter + " exists " + filename + "\n");
					counter++;
					downloaded.setValue(counter);

					continue;
				}
				results.append("Downloading file #" + counter + " of " + userFileCount + " " + filename + "\n");

				URL website = null;
				try {
					website = new URL(backupLocation + filename);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
				ReadableByteChannel rbc = null;
				try {
					rbc = Channels.newChannel(website.openStream());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream((String) filename);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				try {
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

					counter++;


				} catch (IOException e1) {
					e1.printStackTrace();
				}
				downloaded.setValue(counter);
			}

			return null;
		}

		public void done() {
			java.awt.Toolkit.getDefaultToolkit().beep();
			submit.setEnabled(true);
			setCursor(null); //turn off the wait cursor
			System.out.println("Done!\n");
			results.append("Done!\n");
		}
	}

	public class Sites {
		public String name;
		public String sxdUrl;
		public String fileListUrl;
		public String backupLocation;
	}

	public void initialize() {
		// open config file
		Document doc = null;
		String siteName = null;
		sites = new ArrayList<Sites>();

		File configFile = new File("sites.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

		DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		try {
			doc = dBuilder.parse(configFile);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		doc.getDocumentElement().normalize();

		NodeList nodeList = doc.getElementsByTagName("site");

		// read all sites and add them to combo box
		for (int temp = 0; temp < nodeList.getLength(); temp++) {
			Sites site = new Sites();
			Node nNode = nodeList.item(temp);
			NamedNodeMap attributeList = nNode.getAttributes();

			siteName = attributeList.getNamedItem("name").getNodeValue();

			site.name = siteName;
			site.sxdUrl = attributeList.getNamedItem("sypex").getNodeValue();
			site.fileListUrl = attributeList.getNamedItem("filelist").getNodeValue();
			site.backupLocation = attributeList.getNamedItem("backupLocation").getNodeValue();
			sites.add(site);

			sitesBox.addItem(new ComboItem(siteName, Integer.toString(temp)));
		}


		setContentPane(rootPanel);

		rootPanel.setPreferredSize(new Dimension(800, 600));
		pack();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		submit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				submit.setEnabled(false);
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				ProcessDownload task = new ProcessDownload();

				task.execute();
			}
		});

		setVisible(true);
	}
}

