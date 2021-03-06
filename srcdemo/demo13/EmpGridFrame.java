/*
 * EmpGridFrame.java
 *
 * Created on 7 aprile 2007, 22.38
 */

package demo13;


import javax.swing.*;
import org.openswing.swing.client.*;
import java.awt.*;
import org.openswing.swing.table.columns.client.*;
import org.openswing.swing.lookup.client.LookupController;
import java.sql.*;
import java.awt.event.*;
import org.openswing.swing.table.java.*;
import org.openswing.swing.mdi.client.InternalFrame;

/**
 *
 * @author  Administrator
 */
public class EmpGridFrame extends org.openswing.swing.mdi.client.InternalFrame {
    
  private Connection conn = null;
    
    
    /** Creates new form BeanForm */
    public EmpGridFrame(Connection conn,EmpGridFrameController controller) {
        this.conn = conn;
        try {
          initComponents();
          setSize(750,300);
          grid.setController(controller);
          grid.setGridDataLocator(controller);

        }
        catch(Exception e) {
          e.printStackTrace();
        }
    }
    
    
  public void reloadData() {
    grid.reloadData();
  }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        grid = new org.openswing.swing.client.GridControl();
        textColumn1 = new org.openswing.swing.table.columns.client.TextColumn();
        textColumn2 = new org.openswing.swing.table.columns.client.TextColumn();
        textColumn3 = new org.openswing.swing.table.columns.client.TextColumn();
        textColumn4 = new org.openswing.swing.table.columns.client.TextColumn();
        textColumn5 = new org.openswing.swing.table.columns.client.TextColumn();
        buttonsPanel = new javax.swing.JPanel();
        insertButton1 = new org.openswing.swing.client.InsertButton();
        reloadButton1 = new org.openswing.swing.client.ReloadButton();
        deleteButton1 = new org.openswing.swing.client.DeleteButton();
        exportButton1 = new org.openswing.swing.client.ExportButton();
        navigatorBar1 = new org.openswing.swing.client.NavigatorBar();

        grid.getColumnContainer().setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        grid.setDeleteButton(deleteButton1);
        grid.setExportButton(exportButton1);
        grid.setFunctionId("getEmployees");
        grid.setMaxSortedColumns(2);
        grid.setNavBar(navigatorBar1);
        grid.setReloadButton(reloadButton1);
        grid.setValueObjectClassName("demo13.GridEmpVO");
        textColumn1.setColumnName("empCode");
        grid.getColumnContainer().add(textColumn1);

        textColumn2.setColumnName("firstName");
        grid.getColumnContainer().add(textColumn2);

        textColumn3.setColumnName("lastName");
        grid.getColumnContainer().add(textColumn3);

        textColumn4.setColumnName("deptCode");
        grid.getColumnContainer().add(textColumn4);

        textColumn5.setColumnName("deptDescription");
        textColumn5.setPreferredWidth(250);
        grid.getColumnContainer().add(textColumn5);

        getContentPane().add(grid, java.awt.BorderLayout.CENTER);

        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        insertButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertButton1ActionPerformed(evt);
            }
        });

        buttonsPanel.add(insertButton1);

        buttonsPanel.add(reloadButton1);

        buttonsPanel.add(deleteButton1);

        buttonsPanel.add(exportButton1);

        buttonsPanel.add(navigatorBar1);

        getContentPane().add(buttonsPanel, java.awt.BorderLayout.NORTH);

    }
    // </editor-fold>//GEN-END:initComponents

    private void insertButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertButton1ActionPerformed
        new EmpDetailFrameController(this,null,conn);
    }//GEN-LAST:event_insertButton1ActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private org.openswing.swing.client.DeleteButton deleteButton1;
    private org.openswing.swing.client.ExportButton exportButton1;
    private org.openswing.swing.client.GridControl grid;
    private org.openswing.swing.client.InsertButton insertButton1;
    private org.openswing.swing.client.NavigatorBar navigatorBar1;
    private org.openswing.swing.client.ReloadButton reloadButton1;
    private org.openswing.swing.table.columns.client.TextColumn textColumn1;
    private org.openswing.swing.table.columns.client.TextColumn textColumn2;
    private org.openswing.swing.table.columns.client.TextColumn textColumn3;
    private org.openswing.swing.table.columns.client.TextColumn textColumn4;
    private org.openswing.swing.table.columns.client.TextColumn textColumn5;
    // End of variables declaration//GEN-END:variables
    
}
