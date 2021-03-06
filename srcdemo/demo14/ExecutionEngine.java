package demo14;

import java.io.*;
import java.util.zip.*;
import java.util.ArrayList;
import org.openswing.swing.miscellaneous.client.ProgressDialog;
import org.openswing.swing.miscellaneous.client.ProgressEvent;
import org.openswing.swing.wizard.client.WizardPanel;
import javax.swing.JOptionPane;
import org.openswing.swing.client.OptionPane;
import org.openswing.swing.tree.client.*;
import org.openswing.swing.util.client.ClientSettings;
import javax.swing.tree.TreePath;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


/**
 * <p>Title: OpenSwing Framework</p>
 * <p>Description: Class used to perform zip/unzip task.</p>
 * <p>Copyright: Copyright (C) 2006 Mauro Carniel</p>
 * @version 1.0
 */
public class ExecutionEngine {

  private boolean interrupted = false;
  private WizardFrame frame = null;


  /**
   * Check if all input controls are defined.
   */
  public ExecutionEngine(WizardPanel wizardPanel,WizardFrame frame) {
    this.frame = frame;

    if (wizardPanel.getCurrentVisiblePanel().getPanelId().equals("SECOND")) {
      SecondPanel p = (SecondPanel)wizardPanel.getCurrentVisiblePanel();
      // file to decompress...
      File zipFile = new File(p.controlFileName.getText());
      if (!zipFile.isFile() || !zipFile.exists() || !zipFile.getName().endsWith(".zip")) {
        OptionPane.showMessageDialog(frame,"You must specify a zip file","Attention",JOptionPane.WARNING_MESSAGE);
        return;
      }
      File destDir = new File(p.controlDestPath.getText());
      if (!destDir.isDirectory() || !destDir.exists()) {
        OptionPane.showMessageDialog(frame,"You must specify an existing folder","Attention",JOptionPane.WARNING_MESSAGE);
        return;
      }

      try {
        ZipInputStream zip = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry entry = null;
        String name = null;
        byte[] bb = new byte[10000];
        int len = 0;
        FileOutputStream out = null;
        while((entry=zip.getNextEntry())!=null) {
          name = entry.getName();
          if (entry.isDirectory())
            new File(destDir.getAbsolutePath()+"/"+name).mkdirs();
          else {
            out = new FileOutputStream(destDir.getAbsolutePath()+"/"+name);
            while((len = zip.read(bb))>0)
              out.write(bb,0,len);
            out.close();
          }
          zip.closeEntry();
        }
        zip.close();
        System.exit(0);
      }
      catch (Exception ex) {
        OptionPane.showMessageDialog(frame,ex.getMessage(),"Attention",JOptionPane.ERROR_MESSAGE);
      }
    }

    else {
      ThirdPanel p = (ThirdPanel)wizardPanel.getCurrentVisiblePanel();
      // folder to compress...
      File folder = new File(p.controlFolder.getText());
      if (!folder.isDirectory() || !folder.exists()) {
        OptionPane.showMessageDialog(frame,"You must specify an existing folder","Attention",JOptionPane.WARNING_MESSAGE);
        return;
      }
      File destFile = new File(p.controlZip.getText());
      if (destFile.isDirectory() || !destFile.getName().endsWith(".zip")) {
        OptionPane.showMessageDialog(frame,"You must specify a zip file","Attention",JOptionPane.WARNING_MESSAGE);
        return;
      }

      // find out files and sub-folders...
      final ArrayList files = new ArrayList();
      addFiles(files,folder);
      final ProgressDialog d = new ProgressDialog(
           frame,
           "Zip/Unzip Files Wizard",
           "Fetching files...",
           "Searching for files in file system to add to zip file",
           new String[] {"File name","File size"},
           0,
           100,
           false,
           true
      );
      d.setImageName("file.gif");
      d.addCancelButtonListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          interrupted = true;
        }
      });

      try {
        final ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(destFile));

        d.setMaximumValue(files.size());
/*
        // uncomment this to add colored bands...
        for(int i=0;i<files.size();i++)
          d.addColoredBand(i,i+1,new Color(155+(int)(100d*(double)i/(double)files.size()),155+(int)(100d*(double)i/(double)files.size()),155+(int)(100d*(double)i/(double)files.size())));
*/
/*
         // uncomment this to enable color changing according to colored bands, using only one color...
        d.setShowAllBands(false);
*/

        d.setVisible(true);
        interrupted = false;

        new Thread() {
          public void run() {
            compressFiles(files,zip,d);
          }
        }.start();

      }
      catch (Exception ex2) {
        try {
          d.setVisible(false);
          d.dispose();
        }
        catch (Exception ex1) {
        }
      }
    }
  }


  private void compressFiles(ArrayList files,ZipOutputStream zip,ProgressDialog d) {
    try {
      ZipEntry entry = null;
      File name = null;
      byte[] bb = new byte[10000];
      int len = 0;
      FileInputStream in = null;

      for(int i=0;i<files.size();i++) {
        if (interrupted) {
//          d.setVisible(false);
//          d.dispose();
          return;
        }
        name = (File)files.get(i);
        entry = new ZipEntry(name.getPath());
        zip.putNextEntry(entry);
        if (name.isFile()) {
          in = new FileInputStream(name);
          while((len = in.read(bb))>0)
            zip.write(bb,0,len);
          in.close();
        }
        zip.closeEntry();

        d.processProgressEvent(new ProgressEvent(
            i+1,
            "Adding files to zip destination file...",
            "Add all searched files to\n the specified destination zip file",
            new String[] {name.getAbsolutePath(),String.valueOf(name.length())+" bytes"}
        ));

    try {
      Thread.sleep(500);
    }
    catch (InterruptedException ex1) {
    }

      }
      zip.close();
      System.exit(0);
    }
    catch (Exception ex) {
      OptionPane.showMessageDialog(frame,ex.getMessage(),"Attention",JOptionPane.ERROR_MESSAGE);
    }
  }


  /**
   * Add recursiverly a list of files for the specified directory.
   */
  private void addFiles(ArrayList files,File dir) {
    File[] newfiles = dir.listFiles();
    for(int i=0;i<newfiles.length;i++)
      if (newfiles[i].isDirectory())
        addFiles(files,newfiles[i]);
      else
        files.add(newfiles[i]);
  }


}
