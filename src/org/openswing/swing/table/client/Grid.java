package org.openswing.swing.table.client;

import java.util.*;
import java.util.List;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.openswing.swing.message.send.java.*;
import org.openswing.swing.message.receive.java.*;
import javax.swing.border.*;
import org.openswing.swing.table.columns.client.*;
import org.openswing.swing.table.model.client.*;
import org.openswing.swing.client.*;
import org.openswing.swing.logger.client.Logger;
import org.openswing.swing.table.filter.client.*;

import org.openswing.swing.util.client.*;
import org.openswing.swing.export.client.ExportDialog;
import org.openswing.swing.export.java.ExportOptions;
import org.openswing.swing.table.java.*;
import org.openswing.swing.util.client.ClientSettings;
import org.openswing.swing.form.client.Form;
import org.openswing.swing.export.java.ExportToExcel;
import org.openswing.swing.internationalization.java.*;
import org.openswing.swing.util.java.*;
import java.io.FileOutputStream;
import java.io.File;

import java.awt.dnd.*;
import org.openswing.swing.mdi.client.MDIFrame;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;


/**
 * <p>Title: OpenSwing Framework</p>
 * <p>Description: Grid based on VOListTableModel.
 * This class has not  be used directly by the programmer: it's called by GridControl.</p>
 * <p>Copyright: Copyright (C) 2006 Mauro Carniel</p>
 *
 * <p> This file is part of OpenSwing Framework.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the (LGPL) Lesser General Public
 * License as published by the Free Software Foundation;
 *
 *                GNU LESSER GENERAL PUBLIC LICENSE
 *                 Version 2.1, February 1999
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *       The author may be contacted at:
 *           maurocarniel@tin.it</p>
 *
 * @author Mauro Carniel
 * @version 1.0
 */
public class Grid extends JTable
    implements QuickFilterListener, DragSourceListener, DropTargetListener {


  /** TableModel adapter, used to link ValueObjects to TableModel */
  private VOListAdapter modelAdapter = null;

  private VOListTableModel model = null;

  /** TableModel column properties (name, type, etc.) */
  private Column[] colProps = null;

  /** TableColumn linked to the grid */
  private TableColumn[] tableColumnModel = null;

  /** maximum number of sorted columns */
  private int maxSortedColumns = 1;

  /** active cell background color */
  private Color activeCellBackgroundColor = ClientSettings.GRID_ACTIVE_CELL_BACKGROUND;

  /** flag used to enable column reordering */
  private boolean reorderingAllowed = true;

  /** flag used to enable column resizing */
  private boolean resizingAllowed = true;

  /** bottom panel included into the grid; used to view selected row numbers */
  private GridStatusPanel statusPanel = null;

  /** scroll pane which contains the grid */
  private JScrollPane scrollPane = null;

  /** column header border */
  private static final Border columnHeaderBorder = new BevelBorder(BevelBorder.RAISED);

  /** asceding order versus icon */
  private Icon ascSort = new ImageIcon(ClientUtils.getImage(ClientSettings.SORT_DOWN));

  /** desceding order versus icon */
  private Icon descSort = new ImageIcon(ClientUtils.getImage(ClientSettings.SORT_UP));

  /** vertical scrollbar event */
  private AdjustmentEvent adjEvent = null;

  /** flag used inside addNotify to set column headers and the toolbar state */
  private boolean firstTime = true;

  private Grids grids = null;

  private int fromColIndex;

  private int toColIndex;

  private GridController gridController = null;

  /** <code>true</code> if this is the locked grid */
  private boolean lockedGrid;

  /** drag source */
  private DragSource dragSource = new DragSource();

  /** grid identifier, used for DnD */
  private String gridId = null;

  /** drop gestures */
  private DropTarget dropTarget = null;
  private DropTarget dropTarget2 = null;

  /** flag used to define if row height can change for each row, according to image height included in a cell of grid; default value: <code>true</code> */
  private boolean rowHeightFixed = true;


  /**
   * Costructor called by GridControl: programmer never called directly this class.
   * @param colProps TableModel column properties (name, type, etc.)
   * @param statusPanel bottom panel included into the grid; used to view selected row numbers
   * @param fromColIndex column index from which to start to add TableColumn (related to colProps list)
   * @param toColIndex final column index of TableColumn objects to add (related to colProps list), index excluded
   */
  public Grid(
      Grids grids,
      Column[] colProps,
      GridStatusPanel statusPanel,
      int fromColIndex,
      int toColIndex,
      VOListTableModel model,
      VOListAdapter modelAdapter,
      GridController gridController,
      boolean lockedGrid) {
    super();
    this.grids = grids;
    this.modelAdapter = modelAdapter;
    this.model = model;
    this.colProps = colProps;
    this.statusPanel = statusPanel;
    this.fromColIndex = fromColIndex;
    this.toColIndex = toColIndex;
    this.gridController = gridController;
    this.lockedGrid = lockedGrid;
    this.setShowGrid(true);
    this.setAutoResizeMode(this.AUTO_RESIZE_OFF);
    this.setRowHeight(ClientSettings.CELL_HEIGHT);
    this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.setSurrendersFocusOnKeystroke(true);
    this.setBackground(ClientSettings.GRID_CELL_BACKGROUND);
    this.setForeground(ClientSettings.GRID_CELL_FOREGROUND);
    this.setSelectionBackground(ClientSettings.GRID_SELECTION_BACKGROUND);
    this.setSelectionForeground(ClientSettings.GRID_SELECTION_FOREGROUND);


    try {
      this.setModel(model);

      // set column headers, column types and column renderers/editors...
      prepareJTable();

      // storing of TableColumn objects to the purpouse of reuse them in setVisibleColumns method...
      TableColumnModel colsModel = this.getColumnModel();
      tableColumnModel = new TableColumn[colsModel.getColumnCount()];
      for(int i=0;i<colsModel.getColumnCount();i++)
        tableColumnModel[i] = colsModel.getColumn(i);

      // set columns visibility...
      setVisibleColumns();

      // add mouse listener to capture righe mouse click event, used to show the popup menu...
      this.addMouseListener(new RightClickMouseListener());

      // add mouse listener to capture double click event in the selected row...
      this.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount()==2 &&
                     SwingUtilities.isLeftMouseButton(e) &&
                     Grid.this.gridController!=null &&
                     getSelectedRow()!=-1 &&
                     Grid.this.model.getMode()==Consts.READONLY)
            // call grid controller method on another thread
            new Thread() {
              public void run() {
                Grid.this.gridController.doubleClick(getSelectedRow(),Grid.this.model.getObjectForRow(getSelectedRow()));
              }
            }.start();
        }
      });

      // add key listener to capture the ENTER pressed event in the selected row...
      this.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {

          if (!Grid.this.grids.isListenEvent()) {
            e.consume();
            return;
          }

          // ENTER button pressed in the selected row: fire event to the grid controller...
          if (e.getKeyCode()==e.VK_ENTER &&
              Grid.this.gridController!=null &&
              getSelectedRow()!=-1 &&
              Grid.this.model.getMode()==Consts.READONLY)
            // call grid controller method on another thread
            new Thread() {
              public void run() {
                Grid.this.gridController.enterButton(getSelectedRow(),Grid.this.model.getObjectForRow(getSelectedRow()));
              }
            }.start();

          // ENTER button pressed in the selected row: consule event (otherwise the grid will select the next row)...
          if (e.getKeyCode()==e.VK_ENTER)
            e.consume();

          // accelerator key pressed...
          if (e.getKeyCode()==e.VK_Z && e.isControlDown() && (Grid.this.grids.getReloadButton()!=null) && Grid.this.grids.getReloadButton().isEnabled())
            Grid.this.grids.reload();
          else if (e.getKeyCode()==e.VK_S && e.isControlDown() & (Grid.this.grids.getSaveButton()!=null) && Grid.this.grids.getSaveButton().isEnabled())
            Grid.this.grids.save();
          else if (e.getKeyCode()==e.VK_I && e.isControlDown() && (Grid.this.grids.getInsertButton()!=null) && Grid.this.grids.getInsertButton().isEnabled())
            Grid.this.grids.insert();
          else if (e.getKeyCode()==e.VK_E && e.isControlDown() && (Grid.this.grids.getEditButton()!=null) && Grid.this.grids.getEditButton().isEnabled())
            Grid.this.grids.edit();
          else if (e.getKeyCode()==e.VK_D && e.isControlDown() && (Grid.this.grids.getDeleteButton()!=null) && Grid.this.grids.getDeleteButton().isEnabled())
            Grid.this.grids.delete();
          else if (e.getKeyCode()==e.VK_C && e.isControlDown() && (Grid.this.grids.getCopyButton()!=null) && Grid.this.grids.getCopyButton().isEnabled())
            Grid.this.grids.copy();
          else if (e.getKeyCode()==e.VK_X && e.isControlDown() && (Grid.this.grids.getExportButton()!=null) && Grid.this.grids.getExportButton().isEnabled())
            Grid.this.grids.export();

          // (SHIFT) TAB pressed ...
          if (Grid.this.grids.getLockedGrid()!=null &&
              Grid.this.model.getRowCount()>0 &&
              getSelectedRow()!=-1 &&
              (e.getKeyCode()==e.VK_TAB || e.getKeyCode()==e.VK_RIGHT) &&
              e.getModifiers()!=e.SHIFT_MASK &&
              Grid.this.lockedGrid &&
              getSelectedColumn()==getColumnCount()-1) {
            // TAB pressed...
            e.consume();
            Grid.this.grids.setRowSelectionInterval(getSelectedRow(),getSelectedRow());
            Grid.this.grids.getGrid().setColumnSelectionInterval(0,0);
//            ensureRowIsVisible(getSelectedRow());
            Grid.this.grids.getGrid().requestFocus();
          }
          else if (Grid.this.grids.getLockedGrid()!=null &&
              Grid.this.model.getRowCount()>0 &&
              getSelectedRow()<Grid.this.model.getRowCount()-1 &&
              (e.getKeyCode()==e.VK_TAB || e.getKeyCode()==e.VK_RIGHT) &&
              e.getModifiers()!=e.SHIFT_MASK &&
              !Grid.this.lockedGrid &&
              getSelectedColumn()==getColumnCount()-1) {
            // TAB pressed...
            e.consume();
            try {
              Grid.this.grids.setRowSelectionInterval(getSelectedRow() + 1, getSelectedRow() + 1);
            }
            catch (Exception ex) {
            }
            Grid.this.grids.getLockedGrid().setColumnSelectionInterval(0,0);
//            ensureRowIsVisible(getSelectedRow());
            Grid.this.grids.getLockedGrid().requestFocus();
          }
          else if (Grid.this.grids.getLockedGrid()!=null &&
              Grid.this.model.getRowCount()>0 &&
              getSelectedRow()>0 &&
              (e.getModifiers()==e.SHIFT_MASK && e.getKeyCode()==e.VK_TAB || e.getKeyCode()==e.VK_LEFT) &&
              Grid.this.lockedGrid &&
              getSelectedColumn()==0) {
            // TAB pressed...
            e.consume();
            try {
              Grid.this.grids.getGrid().setColumnSelectionInterval(Grid.this.grids.getGrid().getColumnCount()-1,Grid.this.grids.getGrid().getColumnCount()-1);
              Grid.this.grids.setRowSelectionInterval(getSelectedRow()-1, getSelectedRow()-1);
//              ensureRowIsVisible(getSelectedRow()-1);
            }
            catch (Exception ex1) {
            }
            Grid.this.grids.getGrid().requestFocus();
          }
          else if (Grid.this.grids.getLockedGrid()!=null &&
              Grid.this.model.getRowCount()>0 &&
              getSelectedRow()>=0 &&
              (e.getModifiers()==e.SHIFT_MASK && e.getKeyCode()==e.VK_TAB || e.getKeyCode()==e.VK_LEFT) &&
              !Grid.this.lockedGrid &&
              getSelectedColumn()==0) {
            // TAB pressed...
            e.consume();
            Grid.this.grids.setRowSelectionInterval(getSelectedRow(), getSelectedRow());
            Grid.this.grids.getLockedGrid().setColumnSelectionInterval(Grid.this.grids.getLockedGrid().getColumnCount()-1,Grid.this.grids.getLockedGrid().getColumnCount()-1);
            Grid.this.grids.getLockedGrid().requestFocus();
          }
          else if (Grid.this.grids.getLockedGrid()!=null &&
              Grid.this.model.getRowCount()>0 &&
              getSelectedRow()>=0 &&
              (e.getModifiers()==e.SHIFT_MASK && e.getKeyCode()==e.VK_TAB || e.getKeyCode()==e.VK_LEFT) &&
              !Grid.this.lockedGrid &&
              getSelectedColumn()>0) {
            // TAB pressed...
            Grid.this.grids.setRowSelectionInterval(getSelectedRow(), getSelectedRow());
          } else



          // (SHIFT) TAB pressed ...
          if (getMode()!=Consts.READONLY) {
            if (Grid.this.model.getRowCount()>0 &&
                getSelectedRow()!=-1 &&
                e.getKeyCode()==e.VK_TAB &&
                e.getModifiers()!=e.SHIFT_MASK) {
              // TAB pressed and grid is not in read only mode...
              tabPressed(e);
            }
            else if (Grid.this.model.getRowCount()>0 &&
                     getSelectedRow()!=-1 &&
                     e.getKeyCode()==e.VK_TAB &&
                     e.getModifiers()==e.SHIFT_MASK) {
              // SHIFT+TAB pressed and grid is not in read only mode...
              shiftTabPressed(e);
            }
            else if (getMode()==Consts.INSERT &&
                     e.getKeyCode()==e.VK_DOWN &&
                     getSelectedRow()!=-1 &&
                     getSelectedRow()+1<Grid.this.grids.getMaxNumberOfRowsOnInsert() &&
                     getSelectedRow()+1==Grid.this.grids.getCurrentNumberOfNewRows()) {
              // user has pressed DOWN key and the grid is in INSERT mode and
              // the current selected row is the last inserted row and
              // grid is allowed to insert a new row (<max number of rows on insert...)
              Grid.this.grids.setCurrentNumberOfNewRows( Grid.this.grids.getCurrentNumberOfNewRows()+1 );

              // create a new v.o. and add it to the grid model...
              ValueObject vo = null;
              try {
                vo = (ValueObject)Grid.this.model.getValueObjectType().getConstructor(new Class[0]).newInstance(new Object[0]);
                Grid.this.model.insertObjectAt(vo,Grid.this.grids.getCurrentNumberOfNewRows()-1);
                Grid.this.model.getChangedRowIndexes().add(new Integer(Grid.this.grids.getCurrentNumberOfNewRows()-1));
              }
              catch (Exception ex) {
                ex.printStackTrace();
              }
              catch (Error er) {
                er.printStackTrace();
              }

              try {
                // fire create v.o. event to the grid controller: used to fill in the v.o. with default values...
                Grid.this.gridController.createValueObject(vo);

                // check if there exists a combo-box editable column to pre-set...
                for(int i=0;i<Grid.this.colProps.length;i++)
                  if (Grid.this.colProps[i] instanceof ComboColumn &&
                      !((ComboColumn)Grid.this.colProps[i]).isNullAsDefaultValue())
                    Grid.this.model.setValueAt(
                      ((ComboColumn)Grid.this.colProps[i]).getDomain().getDomainPairList()[0].getCode(),
                      Grid.this.grids.getCurrentNumberOfNewRows()-1,
                      Grid.this.model.findColumn(((ComboColumn)Grid.this.colProps[i]).getColumnName())
                    );

              }
              catch (Throwable ex) {
                Logger.error(this.getClass().getName(),"modeChanged","Error while constructing value object '"+Grid.this.model.getValueObjectType().getName()+"'",ex);
              }
              setRowSelectionInterval(Grid.this.grids.getCurrentNumberOfNewRows()-1,0);
              setColumnSelectionInterval(0,0);
              ensureRowIsVisible(Grid.this.grids.getCurrentNumberOfNewRows()-1);

              if (Grid.this.grids.getLockedGrid()!=null) {
                Grid.this.grids.getLockedGrid().setRowSelectionInterval(Grid.this.grids.getCurrentNumberOfNewRows()-1,0);
                Grid.this.grids.getLockedGrid().setColumnSelectionInterval(Grid.this.grids.getCurrentNumberOfNewRows()-1,0);
                Grid.this.grids.getLockedGrid().ensureRowIsVisible(Grid.this.grids.getCurrentNumberOfNewRows()-1);
                Grid.this.grids.getLockedGrid().editCellAt(Grid.this.grids.getCurrentNumberOfNewRows()-1,0);
                Grid.this.grids.getLockedGrid().requestFocus();
              }
              else {
                editCellAt(Grid.this.grids.getCurrentNumberOfNewRows()-1, 0);
                requestFocus();
              }

              e.consume();
            }

            else if (getMode()==Consts.INSERT &&
                     e.getKeyCode()==e.VK_UP &&
                     getSelectedRow()>0 &&
                     getSelectedRow()+1==Grid.this.grids.getCurrentNumberOfNewRows()) {
              // user has pressed UP key and the grid is in INSERT mode and
              // the current selected row is NOT the first inserted and
              // the selected row is the last inserted
              // then check if the selected row's value object is null
              // if it's null, then remove the related row from the grid...

              try {
                ValueObject voToRemove = Grid.this.model.getObjectForRow(getSelectedRow());
                ValueObject clonedVO = (ValueObject) voToRemove.getClass().newInstance();
                Grid.this.gridController.createValueObject(clonedVO);
                boolean notNullValueFound = false;
                Object value = null;
                Object clonedValue = null;
                for(int i=0;i<Grid.this.model.getColumnCount();i++) {
                  value = Grid.this.model.getValueAt(getSelectedRow(),i);
                  if (value!=null) {
                    // check if the value is the "default" value setted by createValueObject method,
                    // by comparing "cloned v.o." attribute with "v.o. to remove" attribute
                    clonedValue = clonedVO.getClass().getMethod("get"+Grid.this.model.getColumnName(i).toUpperCase().charAt(0)+Grid.this.model.getColumnName(i).substring(1),new Class[0]).invoke(clonedVO,new Object[0]);
                    if (!value.equals(clonedValue) &&
                        !(Grid.this.colProps[i] instanceof ComboColumn && !((ComboColumn)Grid.this.colProps[i]).isNullAsDefaultValue()) &&
                        !(value.equals("") && clonedValue==null)
                    ) {
                      notNullValueFound = true;
                      break;
                    }
                  }
                }
                if (!notNullValueFound) {
                  Grid.this.grids.setCurrentNumberOfNewRows( Grid.this.grids.getCurrentNumberOfNewRows()-1 );
                  Grid.this.model.removeObjectAt(Grid.this.grids.getCurrentNumberOfNewRows());
                  Grid.this.model.getChangedRowIndexes().remove(Grid.this.model.getChangedRowIndexes().size()-1);
                  setRowSelectionInterval(Grid.this.grids.getCurrentNumberOfNewRows()-1,0);
                  setColumnSelectionInterval(0,0);
                  ensureRowIsVisible(Grid.this.grids.getCurrentNumberOfNewRows()-1);

                  if (Grid.this.grids.getLockedGrid()!=null) {
                    Grid.this.grids.getLockedGrid().setRowSelectionInterval(Grid.this.grids.getCurrentNumberOfNewRows()-1,0);
                    Grid.this.grids.getLockedGrid().setColumnSelectionInterval(Grid.this.grids.getCurrentNumberOfNewRows()-1,0);
                    Grid.this.grids.getLockedGrid().ensureRowIsVisible(Grid.this.grids.getCurrentNumberOfNewRows()-1);
                    Grid.this.grids.getLockedGrid().editCellAt(Grid.this.grids.getCurrentNumberOfNewRows()-1,0);
                    Grid.this.grids.getLockedGrid().requestFocus();
                  }
                  else {
                    editCellAt(Grid.this.grids.getCurrentNumberOfNewRows()-1, 0);
                    requestFocus();
                  }

                  e.consume();
                }
              }
              catch (Exception ex2) {
                ex2.printStackTrace();
              }

            }

          } else

          // navigation button pressed...
          if (getMode()==Consts.READONLY &&
              Grid.this.grids.getNavBar()!=null &&
              (e.getKeyCode()==e.VK_UP ||
               e.getKeyCode()==e.VK_DOWN ||
               e.getKeyCode()==e.VK_PAGE_UP ||
               e.getKeyCode()==e.VK_PAGE_DOWN)) {
            if (e.getKeyCode()==e.VK_UP && getSelectedRow()>0)
                Grid.this.grids.getNavBar().prevButton_actionPerformed(null);
            else if (e.getKeyCode()==e.VK_UP && getSelectedRow()==0 && Grid.this.grids.getNavBar().isPrevButtonEnabled())
              Grid.this.grids.getNavBar().prevButton_actionPerformed(null);
            else if (e.getKeyCode()==e.VK_DOWN && getSelectedRow()<Grid.this.model.getRowCount()-1)
              Grid.this.grids.getNavBar().nextButton_actionPerformed(null);
            else if (e.getKeyCode()==e.VK_DOWN && getSelectedRow()==Grid.this.model.getRowCount()-1 && Grid.this.grids.getNavBar().isNextButtonEnabled())
              Grid.this.grids.getNavBar().nextButton_actionPerformed(null);
            else if (e.getKeyCode()==e.VK_PAGE_UP && getSelectedRow()>0) {
              java.awt.Rectangle r = getCellRect(getSelectedRow(), 0, false);
              int delta = (r.y-getVisibleRect().height)/(getRowHeight()+getRowMargin())+1;
              setRowSelectionInterval(Math.max(0,delta),Math.max(0,delta));
              ensureRowIsVisible(getSelectedRow());
              if (getSelectedRow()<Grid.this.model.getRowCount()-1 || Grid.this.grids.isMoreRows())
                Grid.this.grids.getNavBar().setLastRow(false);
              if (getSelectedRow()==0 && Grid.this.grids.getLastIndex()==Grid.this.model.getRowCount()-1)
                Grid.this.grids.getNavBar().setFirstRow(true);
            }
            else if (e.getKeyCode()==e.VK_PAGE_UP && getSelectedRow()==0 && Grid.this.grids.getNavBar().isPrevButtonEnabled()) {
              if (Grid.this.grids.getLastIndex()==Grid.this.model.getRowCount() -1)
                Grid.this.grids.getNavBar().setFirstRow(true);
              else
                Grid.this.grids.getNavBar().prevButton_actionPerformed(null);
            } else if (e.getKeyCode()==e.VK_PAGE_DOWN && getSelectedRow()<Grid.this.model.getRowCount()-1) {
              java.awt.Rectangle r = getCellRect(getSelectedRow(), 0, false);
              int delta = (r.y+getVisibleRect().height)/(getRowHeight()+getRowMargin());
              setRowSelectionInterval(Math.min(Grid.this.model.getRowCount()-1,delta),Math.min(Grid.this.model.getRowCount()-1,delta));
              ensureRowIsVisible(getSelectedRow());
              if (getSelectedRow()>0)
                Grid.this.grids.getNavBar().setFirstRow(false);
              if (getSelectedRow()==Grid.this.model.getRowCount()-1 && !Grid.this.grids.isMoreRows())
                Grid.this.grids.getNavBar().setLastRow(true);
            }
            else if (e.getKeyCode()==e.VK_PAGE_DOWN && getSelectedRow()==Grid.this.model.getRowCount()-1 && Grid.this.grids.getNavBar().isNextButtonEnabled()) {
              if (!Grid.this.grids.isMoreRows())
                Grid.this.grids.getNavBar().setLastRow(true);
              else
                Grid.this.grids.getNavBar().nextButton_actionPerformed(null);
            }
            e.consume();
          }
          else if (Grid.this.lockedGrid && getMode()==Consts.READONLY && Grid.this.grids.getNavBar()==null &&
              (e.getKeyCode()==e.VK_PAGE_UP ||
               e.getKeyCode()==e.VK_PAGE_DOWN)) {
            if (e.getKeyCode()==e.VK_PAGE_UP && getSelectedRow()>0) {
              java.awt.Rectangle r = getCellRect(getSelectedRow(), 0, false);
              int delta = (r.y-getVisibleRect().height)/(getRowHeight()+getRowMargin())+1;
              setRowSelectionInterval(Math.max(0,delta),Math.max(0,delta));
              ensureRowIsVisible(getSelectedRow());
            } else if (e.getKeyCode()==e.VK_PAGE_DOWN && getSelectedRow()<Grid.this.model.getRowCount()-1) {
              java.awt.Rectangle r = getCellRect(getSelectedRow(), 0, false);
              int delta = (r.y+getVisibleRect().height)/(getRowHeight()+getRowMargin());
              setRowSelectionInterval(Math.min(Grid.this.model.getRowCount()-1,delta),Math.min(Grid.this.model.getRowCount()-1,delta));
              ensureRowIsVisible(getSelectedRow());
            }
            e.consume();
          }

        }
      });

      // set column headers...
      prepareHeader();

    }
    catch (Throwable t) {
      Logger.error(this.getClass().getName(),"Grid","Error while constructing grid.",t);
    }
  }


  /**
   * Prepare a <code>JTable</code> for use with this model.
   * This method creates the necessary columns in the <code>JTable</code> and assigns
   * the appropriate cell renderers and editors (as provided by the attribute
   * adapter) for each column.
   */
  public final void prepareJTable() {
    TableColumnModel cmodel = this.getColumnModel();
    int cols = cmodel.getColumnCount();

    for(int i = 0; i < cols; i++) {
      TableColumn tc = cmodel.getColumn(i);
      if (i>=fromColIndex && i<toColIndex) {
        tc.setCellRenderer(modelAdapter.getCellRenderer(i));
        tc.setCellEditor(modelAdapter.getCellEditor(i));
      }

      int w = modelAdapter.getFieldPreferredWidth(i);
      if(w > 0)
        tc.setPreferredWidth(w);

      w = modelAdapter.getFieldMinWidth(i);
      if(w > 0)
        tc.setMinWidth(w);

      w = modelAdapter.getFieldMaxWidth(i);
      if(w > 0)
        tc.setMaxWidth(w);
    }
  }


  /**
   * Method redefined to store the current selected cell when user clicks directly in the cell witi the mouse.
   */
    public boolean editCellAt(int row, int column, EventObject e) {
      setRowSelectionInterval(row,row);
      setColumnSelectionInterval(column,column);
      return super.editCellAt(row, column, e);
    }


    /**
     * Method called when TAB is pressed and grid is not in read only mode...
     */
    private void tabPressed(KeyEvent e) {
      int colIndex = getSelectedColumn();
      if (colIndex==-1) {
          e.consume();
          return;
      }

//      if (lockedGrid && colIndex+1==getColumnCount()) {
//        int row = getSelectedRow();
//        grids.getGrid().setRowSelectionInterval(
//          row>0?row-1:0,
//          row>0?row-1:0
//        );
//        grids.getGrid().setColumnSelectionInterval(0,0);
//        grids.getGrid().ensureRowIsVisible(grids.getGrid().getSelectedRow());
//        grids.getGrid().requestFocus();
//        return;
//      }
//
//      if (!lockedGrid && colIndex+1==getColumnCount() && grids.getLockedGrid()!=null) {
//        grids.getLockedGrid().requestFocus();
//        return;
//      }

      if (colIndex+1==getColumnCount() &&
          getSelectedRow()<model.getRowCount()-1) {
        colIndex = -1;
        setRowSelectionInterval(getSelectedRow()+1,getSelectedRow()+1);
        ensureRowIsVisible(getSelectedRow());
      }
      while(colIndex+1<getColumnCount() &&
            !isCellEditable(getSelectedRow(),colIndex+1)) {
        colIndex++;
      }
      if (colIndex+1<getColumnCount()) {
        ensureRowIsVisible(getSelectedRow());
        setColumnSelectionInterval(colIndex,colIndex);
        setRowSelectionInterval(getSelectedRow(),getSelectedRow());
      } else {
        if (getSelectedRow()<model.getRowCount()-1) {
          setRowSelectionInterval(getSelectedRow()+1,getSelectedRow()+1);
          setColumnSelectionInterval(0,0);
          ensureRowIsVisible(getSelectedRow());
          if (!isCellEditable(getSelectedRow(),0))
            tabPressed(e);
          else
            e.consume();
        }
        else {
          e.consume();
          return;
        }
      }
    }


    /**
     * Method called when SHIFT+TAB are pressed and grid is not in read only mode...
     */
    private void shiftTabPressed(KeyEvent e) {
      int colIndex = getSelectedColumn();
      if (colIndex==-1) {
        e.consume();
        return;
      }

//      if (!lockedGrid && colIndex==0 && grids.getLockedGrid()!=null) {
//        grids.getLockedGrid().setRowSelectionInterval(grids.getLockedGrid().getSelectedRow()+1,grids.getLockedGrid().getSelectedRow()+1);
//        grids.getLockedGrid().setColumnSelectionInterval(grids.getLockedGrid().getColumnCount()-1,grids.getLockedGrid().getColumnCount()-1);
//        grids.getLockedGrid().ensureRowIsVisible(grids.getLockedGrid().getSelectedRow());
//        grids.getLockedGrid().requestFocus();
//        return;
//      }
//
//      if (lockedGrid && colIndex==0) {
//        grids.getGrid().setRowSelectionInterval(grids.getGrid().getSelectedRow()+1,grids.getGrid().getSelectedRow()+1);
//        grids.getGrid().setColumnSelectionInterval(grids.getGrid().getColumnCount()-1,grids.getGrid().getColumnCount()-1);
//        grids.getGrid().ensureRowIsVisible(grids.getGrid().getSelectedRow());
//        grids.getGrid().requestFocus();
//        return;
//      }

      if (colIndex==0 && getSelectedRow()>0) {
        colIndex = getColumnCount()-1;
        setRowSelectionInterval(getSelectedRow()-1,getSelectedRow()-1);
        setColumnSelectionInterval(colIndex,colIndex);
      }
      while(colIndex>0 &&
            !isCellEditable(getSelectedRow(),colIndex-1)) {
        colIndex--;
      }

//      if (lockedGrid && colIndex==0) {
//        grids.getGrid().setRowSelectionInterval(grids.getGrid().getSelectedRow()+1,grids.getGrid().getSelectedRow()+1);
//        grids.getGrid().setColumnSelectionInterval(grids.getGrid().getColumnCount()-1,grids.getGrid().getColumnCount()-1);
//        grids.getGrid().ensureRowIsVisible(grids.getGrid().getSelectedRow());
//        grids.getGrid().requestFocus();
//        return;
//      }

      if (colIndex>=0) {
        setColumnSelectionInterval(colIndex,colIndex);
      }
      else {
        if (getSelectedRow()>0) {

//          if (lockedGrid && colIndex==0) {
//            grids.getGrid().setRowSelectionInterval(grids.getGrid().getSelectedRow()+1,grids.getGrid().getSelectedRow()+1);
//            grids.getGrid().setColumnSelectionInterval(grids.getGrid().getColumnCount()-1,grids.getGrid().getColumnCount()-1);
//            grids.getGrid().ensureRowIsVisible(grids.getGrid().getSelectedRow());
//            grids.getGrid().requestFocus();
//            return;
//          }

          setRowSelectionInterval(getSelectedRow()-1,getSelectedRow()-1);
          setColumnSelectionInterval(colIndex,colIndex);
          shiftTabPressed(e);
        } else {
          e.consume();
          return;
        }
      }
  }



  /**
   * Add vertical listener to capture grid vertical scrolling events.
   */
  private void createVerticalScrollBarListener(JScrollPane scrollPane) {
    JScrollBar vScrollbar = vScrollbar=scrollPane.getVerticalScrollBar();
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
    vScrollbar.setUnitIncrement(getRowHeight());
    vScrollbar.setBlockIncrement(getRowHeight());
    vScrollbar.addAdjustmentListener(new AdjustmentListener() {
      public void adjustmentValueChanged(AdjustmentEvent e) {
        adjEvent = e;
      }
    });
    VerticalScrollBarMouseListener vsListener = new VerticalScrollBarMouseListener();
    vScrollbar.addMouseListener(vsListener); // mouse lister alla scrollbar
    vScrollbar.getComponents()[0].addMouseListener(vsListener); // mouse lister al pulsante su della scrollbar
    vScrollbar.getComponents()[1].addMouseListener(vsListener); // mouse lister al pulsante giu' della scrollbar
  }


  /**
   * Add mouse listener in vertical scrollbar (to capture mouse click events in scrollbar buttons).
   */
  class VerticalScrollBarMouseListener extends MouseAdapter {

    public void mouseReleased(MouseEvent e){
      if (javax.swing.SwingUtilities.isLeftMouseButton(e)){
          scrollMov((JScrollBar)adjEvent.getAdjustable());
      }
    }
  }


  /**
   * Method called on scrolling grid: grid must fetch a new block of rows.
   */
  private void scrollMov(JScrollBar scrollBar){
    if (getMode()!=Consts.READONLY)
      return;

    if (!grids.isListenEvent())
      return;

    int scrollableZoneSize = scrollBar.getMaximum()-scrollBar.getVisibleAmount();
    if ((scrollBar.getValue()>scrollableZoneSize/4*3) && grids.isMoreRows()) {
      // scrolling table over 75% of rows (near the end)
      setRowSelectionInterval(model.getRowCount()-1,model.getRowCount()-1);
      grids.nextRow();
      setRowSelectionInterval(model.getRowCount()/2,model.getRowCount()/2);
      ensureRowIsVisible(getSelectedRow());
    } else if ((scrollBar.getValue()< scrollableZoneSize/4) &&
               grids.getLastIndex()+1>Math.min(ClientSettings.BLOCK_SIZE,model.getRowCount())) {
      // scrolling table on 25% of rows (near the beginning)
      setRowSelectionInterval(0,0);
      grids.previousRow();
      setRowSelectionInterval(model.getRowCount()/2,model.getRowCount()/2);
      ensureRowIsVisible(getSelectedRow());
    }
  }


  /**
   * Set column headers.
   */
  private void prepareHeader() {
    for(int i=0;i<tableColumnModel.length;i++) {
      tableColumnModel[i].setHeaderRenderer(new TableColumnHeaderRenderer(this.colProps[i].getHeaderTextAlignment()));
    }
    JTableHeader th = this.getTableHeader();
    th.setPreferredSize(new Dimension(th.getPreferredSize().width,ClientSettings.HEADER_HEIGHT));
    th.setReorderingAllowed(reorderingAllowed);
    th.setResizingAllowed(resizingAllowed);
    th.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e)
        {

        // mouse click event is ignored if grid is not in read only mode or is not enabled...
        if(!isEnabled() || model.getMode()!=Consts.READONLY)
          return;

        TableColumnModel columnModel = getColumnModel();
        int viewColumnIndex = columnModel.getColumnIndexAtX(e.getX());
        int modelColumnIndex = convertColumnIndexToModel(viewColumnIndex);
        if(e.getClickCount() == 1 && modelColumnIndex != -1) {
          refreshIconsAndSort(modelColumnIndex);
          return;
        }
      }
    });
    setOrderList();
  }


  /**
   * Method called when user clicks on a column header to sort the column:
   * it will control if the specified column is sortable and after that, it will reload data on grid,
   * according to the new sorting settings; sorting icons will be updated.
   * @param modelColumnIndex column model index of the column to sort
   */
  private void refreshIconsAndSort(int modelColumnIndex) {
    try {
      Column colProp = colProps[modelColumnIndex];
      if (!colProp.isColumnSortable()) {
        // view warning message ...
        JOptionPane.showMessageDialog(
            ClientUtils.getParentFrame(this),
            ClientSettings.getInstance().getResources().getResource("This column is not sorteable"),
            ClientSettings.getInstance().getResources().getResource("Sorting not allowed"),
            JOptionPane.WARNING_MESSAGE
        );
        return;
      }

      // (max) sorting order of columns...
      int maxSortingOrder = -1;

      // determine the number of current sorted columns...
      int currentColSorted = 0;
      for(int i=0;i<colProps.length;i++)
        if (colProps[i].isColumnSortable() &&
            !colProps[i].getSortVersus().equals(Consts.NO_SORTED)) {
          currentColSorted++;
          maxSortingOrder = Math.max(maxSortingOrder,colProps[i].getSortingOrder());
        }

      // determine the versus for the specified column (if sorting is applicable)...
      String currentSortVersus = colProp.getSortVersus();
      if (currentSortVersus.equals(Consts.NO_SORTED))
        currentSortVersus = Consts.ASC_SORTED;
      else if (currentSortVersus.equals(Consts.ASC_SORTED))
        currentSortVersus = Consts.DESC_SORTED;
      else if (currentSortVersus.equals(Consts.DESC_SORTED))
        currentSortVersus = Consts.NO_SORTED;

      // determine if it's possible to sort by the specified column...
      if (colProp.getSortVersus().equals(Consts.NO_SORTED) &&
          !currentSortVersus.equals(Consts.NO_SORTED) &&
          currentColSorted>=maxSortedColumns) {
        // view warning message...
        JOptionPane.showMessageDialog(
            ClientUtils.getParentFrame(this),
            ClientSettings.getInstance().getResources().getResource("Maximum number of sorted columns"),
            ClientSettings.getInstance().getResources().getResource("Sorting not applicable"),
            JOptionPane.WARNING_MESSAGE
        );
        return;
      }
      colProp.setSortVersus(currentSortVersus);
      colProp.setSortingOrder(maxSortingOrder+1);
      getTableHeader().repaint();
      setOrderList();
      grids.reload();
    }
    catch (Exception ex) {
      Logger.error(this.getClass().getName(),"refreshIconsAndSort","Error while loading data on sorting",ex);
    }
  }


  /**
   * Method called when the grid will be view, to correctly initialize columns which are declared sorted.
   */
  private void setOrderList() {
    Integer[] aux = new Integer[colProps.length];
    for(int i=0;i<colProps.length;i++)
      if (!colProps[i].getSortVersus().equals(Consts.NO_SORTED))
        aux[colProps[i].getSortingOrder()] = new Integer(i);

    grids.getCurrentSortedColumns().clear();
    grids.getCurrentSortedVersusColumns().clear();

    for(int i=0;i<aux.length;i++)
      if (aux[i]!=null) {
        if (colProps[ ( (Integer) aux[i]).intValue()].getSortVersus().equals(Consts.ASC_SORTED)) {
          grids.getCurrentSortedColumns().add(colProps[ ( (Integer) aux[i]).intValue()].getColumnName());
          grids.getCurrentSortedVersusColumns().add(Consts.ASC_SORTED);
        }
        else if (colProps[ ( (Integer) aux[i]).intValue()].getSortVersus().equals(Consts.DESC_SORTED)) {
          grids.getCurrentSortedColumns().add(colProps[ ( (Integer) aux[i]).intValue()].getColumnName());
          grids.getCurrentSortedVersusColumns().add(Consts.DESC_SORTED);
        }
      }
  }


  /**
   * Configure quick filter.
   */
  private void configQuickFilter() {
    int modelColIndex = this.getSelectedColumn()==-1 ? -1 : this.getColumnModel().getColumn(this.getSelectedColumn()).getModelIndex();

    if (modelColIndex!=-1 &&
        colProps[modelColIndex].isColumnFilterable() &&
        colProps[modelColIndex].getColumnType()!=Column.TYPE_CHECK) {
      int filterType=grids.getFilterPanel().FILTER_TYPE_VALUE;
      switch (colProps[modelColIndex].getColumnType()) {
        case Column.TYPE_DATE :
        case Column.TYPE_DATE_TIME :
        case Column.TYPE_TIME :
          filterType=grids.getFilterPanel().FILTER_TYPE_RANGE;
        break;
        default:
          filterType=grids.getFilterPanel().FILTER_TYPE_VALUE;
      }
      if (grids.getQuickFilterValues().get(colProps[modelColIndex].getColumnName())!=null)
        grids.getRemovefilterItem().setVisible(true);
       else
        grids.getRemovefilterItem().setVisible(false);

       grids.setFilterPanel( new QuickFilterPanel(
           this,
           filterType,
           colProps[modelColIndex],
           this.getSelectedRow()>=0?this.getModel().getValueAt(this.getSelectedRow(),modelColIndex):null
       ));
       grids.getFilterPanel().setVisible(colProps[modelColIndex].isColumnFilterable());
    } else
      grids.setFilterPanel(null);
  }


  /**
   * <p>Title: Open Swing</p>
   * <p>Description: Inner class used to set the column header content,
   * according to sorting settings.</p>
   * <p>Copyright: Copyright (C) 2006 Mauro Carniel</p>
   * @author Mauro Carniel
   * @version 1.0
   */
  class TableColumnHeaderRenderer extends JPanel implements TableCellRenderer {

    private Icon sortIcon;
    private JLabel l_text, l_icon;

    public TableColumnHeaderRenderer(int headerTextAlignment) {
      setOpaque(false);
      setBorder(columnHeaderBorder);
      setLayout(new BorderLayout(0, 0));
      l_text = new JLabel();
      l_text.setVerticalTextPosition(SwingConstants.CENTER);
      l_text.setHorizontalAlignment(headerTextAlignment);
      l_text.setOpaque(false);
      add(l_text, BorderLayout.CENTER);
      l_icon = new JLabel("");
      l_icon.setOpaque(false);
      add(l_icon, BorderLayout.EAST);
    }


    public void setIcon(Icon icon) {
      l_icon.setIcon(icon);
    }


    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int viewColumnIndex) {
     int columnModelIndex = table.convertColumnIndexToModel(viewColumnIndex);
     setIcon(null);
     for(int i=0;i<grids.getCurrentSortedColumns().size();i++)
       if (grids.getCurrentSortedColumns().get(i).equals(colProps[columnModelIndex].getColumnName())) {
         // current column is sorted...
         if (grids.getCurrentSortedVersusColumns().get(i).equals(Consts.ASC_SORTED))
           setIcon(ascSort);
         else
           setIcon(descSort);

       }
      l_text.setText(value.toString());
      return this;
    }


    public Font getHeaderTextFont() {
      return(l_text.getFont());
    }


    public void setHeaderTextFont(Font font) {
      l_text.setFont(font);
      l_text.repaint();
    }


    public Color getHeaderTextForeground() {
      return(l_text.getForeground());
    }


    public void setHeaderTextForeground(Color color) {
      l_text.setForeground(color);
      l_text.repaint();
    }


    public int getHeaderTextAlignment() {
      return(l_text.getHorizontalAlignment());
    }


    public void setHeaderTextAlignment(int alignment) {
      l_text.setHorizontalAlignment(alignment);
    }


  } //TableColumnHeaderRenderer


  /**
   * Method called when user right clicks with the mouse on the grid: it will view the popup menu.
   */
  private void showPopupMenu(final int x,final int y) {
    try {
      final Point tablexy=this.getLocationOnScreen();
      final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

      grids.getPopup().removeAll();

      // add quick filter to popup menu...
      configQuickFilter();
      if (grids.getFilterPanel()!=null) {
        grids.getFilterPanel().setParentPopup(grids.getPopup());
        grids.getPopup().add(grids.getFilterPanel());
        // add menu item to remove filter setting on the popup menu...
        grids.getRemovefilterItem().setToolTipText(ClientSettings.getInstance().getResources().getResource("Remove Filter"));
        grids.getRemovefilterItem().removeAll();
        grids.getRemovefilterItem().addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            int modelColIndex = getSelectedColumn()==-1 ? -1 :getColumnModel().getColumn(getSelectedColumn()).getModelIndex();
            if (modelColIndex==-1) {
              int xOverflow = x+(int)tablexy.getX()+grids.getPopup().getWidth()-(int)screenSize.getWidth();
              int yOverflow = y+(int)tablexy.getY()+grids.getPopup().getHeight()-(int)screenSize.getHeight();
              int popupX = xOverflow>0?x-xOverflow-20:x;
              int popupY = yOverflow>0?y-yOverflow-20:y;
              modelColIndex = columnAtPoint(new Point(popupX,popupY));
            }

            if (modelColIndex!=-1) {
              removeQuickFilter(modelAdapter.getFieldName(modelColIndex));
            }
          }
        });
        grids.getPopup().add(grids.getRemovefilterItem());

        grids.getPopup().add(new JSeparator());
      }

      // add optional command to popup menu...
      for(int i=0;i<grids.getPopupCommands().size();i++)
        grids.getPopup().add((JMenuItem)grids.getPopupCommands().get(i));
      if (grids.getPopupCommands().size()>0)
        grids.getPopup().add(new JSeparator());

      // add selectable columns to popup menu...
      JCheckBoxMenuItem cbMenuItem;
      for(int i=0;i<colProps.length;i++)
        if (colProps[i].isColumnSelectable()) {
          if (colProps[i].getHeaderColumnName()!=null &&
              colProps[i].getHeaderColumnName().length()>0)
            cbMenuItem = new JCheckBoxMenuItem(ClientSettings.getInstance().getResources().getResource(colProps[i].getHeaderColumnName()));
          else
            cbMenuItem = new JCheckBoxMenuItem(ClientSettings.getInstance().getResources().getResource(colProps[i].getColumnName()));
          if (colProps[i].isColumnVisible())
            cbMenuItem.setState(true);
          cbMenuItem.addActionListener(new CheckboxMenuItem(i));
          grids.getPopup().add(cbMenuItem);
        }

      int xOverflow = x+(int)tablexy.getX()+grids.getPopup().getWidth()-(int)screenSize.getWidth();
      int yOverflow = y+(int)tablexy.getY()+grids.getPopup().getHeight()-(int)screenSize.getHeight();
      int popupX = xOverflow>0?x-xOverflow-20:x;
      int popupY = yOverflow>0?y-yOverflow-20:y;
      grids.getPopup().show(this,popupX,popupY);
    }
    catch (Throwable ex) {
      Logger.error(this.getClass().getName(),"showPopupMenu","Error while constructing grids.getPopup() menu.",ex);
      ex.printStackTrace();
    }
  }


  class CheckboxMenuItem implements ActionListener {

    private int columnIndex = -1;

    public CheckboxMenuItem(int columnIndex) {
      this.columnIndex = columnIndex;
    }

    public void actionPerformed(ActionEvent e) {
      colProps[columnIndex].setColumnVisible(!colProps[columnIndex].isColumnVisible());
      setVisibleColumn(columnIndex,colProps[columnIndex].isColumnVisible());
    }
  }


  /**
   * Set column visibility for all grid columns.
   */
  private final void setVisibleColumns() {
    // tolgo tutte le colonne...
    TableColumnModel colsModel = this.getColumnModel();
    while(colsModel.getColumnCount()>0)
      colsModel.removeColumn(colsModel.getColumn(0));
    // aggiungo le colonne dichiarate visibili...
    for(int i=fromColIndex;i<toColIndex;i++) {
      if (colProps[i].isColumnVisible())
        this.addColumn(tableColumnModel[i]);
    }
  }


  /**
   * Set (in)visibility state of the specified column.
   * @param columnModelIndex column model index
   * @param colVisible (in)visibility state for the specified column
   */
  public final void setVisibleColumn(int columnModelIndex,boolean colVisible) {
    TableColumnModel colsModel = this.getColumnModel();
    int columnViewIndex = this.convertColumnIndexToView(columnModelIndex);
    if (!colVisible && columnViewIndex!=-1)
      colsModel.removeColumn(colsModel.getColumn(columnViewIndex));
    else if (colVisible && columnViewIndex==-1) {
      setVisibleColumns();
    }
  }


  /**
   * @return table model of the grid
   */
  public final VOListTableModel getVOListTableModel() {
    return model;
  }


  /**
   * Set maximum number of sorted columns.
   * @param maxSortedColumns maximum number of sorted columns
   */
  public final void setMaxSortedColumns(int maxSortedColumns) {
    this.maxSortedColumns = maxSortedColumns;
  }


  /**
   * Called whenever the value of the selection changes.
   * @param e grid selection event
   */
  public final void valueChanged(ListSelectionEvent e) {
    super.valueChanged(e);

    if (model!=null && getMode()==Consts.INSERT) {
      // if grid is in insert mode then row selection is always forced to the inserted rows...
      if (getSelectedRow()>=grids.getCurrentNumberOfNewRows()) {
        setRowSelectionInterval(
            Math.max(0,grids.getCurrentNumberOfNewRows()-1),
            Math.max(0,grids.getCurrentNumberOfNewRows()-1)
        );
        grids.setRowSelectionInterval(
            Math.max(0,grids.getCurrentNumberOfNewRows()-1),
            Math.max(0,grids.getCurrentNumberOfNewRows()-1)
        );
        return;
      }
    }

    if (grids!=null && grids.getLockedGrid()!=null)
      grids.setRowSelectionInterval(this.getSelectedRow(),this.getSelectedRow());

    // skip additional events...
    if (e.getValueIsAdjusting())
      return;

    if (model!=null && getMode()==Consts.READONLY) {
      if (this.getSelectedRows().length>1) {
        // toolbar disabilitation...
        if (grids.getNavBar()!=null)
          grids.getNavBar().setEnabled(false);
        if (grids.getEditButton()!=null)
          grids.getEditButton().setEnabled(false);
        grids.setGenericButtonsEnabled(false);
      }
      else {
        if (grids.getEditButton() != null)
          grids.getEditButton().setEnabled(true);
        grids.setGenericButtonsEnabled(true);
      }
    }

    if (gridController!=null &&
        this.getSelectedRows().length==1 &&
        this.getSelectedColumn()!=-1) {
      // fire cell selected event...
      int columnIndex = this.convertColumnIndexToModel(this.getSelectedColumn());
      gridController.selectedCell(
          e.getFirstIndex(), columnIndex,
          model.getColumnName(columnIndex),
          model.getObjectForRow(e.getFirstIndex())
      );
      // update navigation toolbar state...
      if (getMode()==Consts.READONLY && (grids.getNavBar()!=null)) {
        if (grids.getLastIndex()+1==Math.min(model.getRowCount(),ClientSettings.BLOCK_SIZE) &&
            this.getSelectedRow()==0)
          grids.getNavBar().setFirstRow(true);
        else
          grids.getNavBar().setFirstRow(false);
        if (!grids.isMoreRows() && this.getSelectedRow()==model.getRowCount()-1)
          grids.getNavBar().setLastRow(true);
        else
          grids.getNavBar().setLastRow(false);
      }
    }

    if (statusPanel!=null) {
      // update status bar content...
      if (this.getSelectedRows().length==0)
        statusPanel.setText("");
      else if (this.getSelectedRows().length==1)
        statusPanel.setText(ClientSettings.getInstance().getResources().getResource("Selected Row")+": "+(grids.getCurrentNumberOfNewRows()+grids.getLastIndex()-model.getRowCount()+this.getSelectedRow()+2));
      else if (this.getSelectedRows().length>1 && getMode()==Consts.READONLY) {
        String rows = "";
        for(int i=0;i<this.getSelectedRows().length;i++)
          rows += (this.getSelectedRows()[i]+2+grids.getCurrentNumberOfNewRows()+grids.getLastIndex()-model.getRowCount())+", ";
        rows = rows.substring(0,rows.length()-2);
        statusPanel.setText(ClientSettings.getInstance().getResources().getResource("Selected Rows")+": "+rows);
      }
      else statusPanel.setText("");
    }

    try {
      ensureRowIsVisible(getSelectedRow());
    }
    catch (Exception ex) {
    }

    // fire row selected event...
    if (gridController!=null && this.getSelectedRow()!=-1)
      gridController.rowChanged(this.getSelectedRow());
  }


  /**
   * Method overrided to ensure that the current selected cell is always visible.
   * @param e ListSelectionEvent
   */
  public void columnSelectionChanged(ListSelectionEvent e) {
    super.columnSelectionChanged(e);
    ensureRowIsVisible(getSelectedRow());

    revalidate();
    repaint();
    getTableHeader().revalidate();
    getTableHeader().repaint();

  }


  /**
   * @return background color for the selected cell
   */
  public final Color getActiveCellBackgroundColor() {
    return activeCellBackgroundColor;
  }


  /**
   * Set background color for the selected cell.
   * @param activeCellBackgroundColor background color for the selected cell
   */
  public final void setActiveCellBackgroundColor(Color activeCellBackgroundColor) {
    this.activeCellBackgroundColor = activeCellBackgroundColor;
  }


  /**
   * @return current grid mode; possible values: Consts.READ_ONLY, Consts.EDIT, Consts.INSERT
   */
  public final int getMode() {
    return model.getMode();
  }


  /**
   * Set if it is allowed to reorder columns.
   * @param reorderingAllowed flag used to set columns reordering
   */
  public final void setReorderingAllowed(boolean reorderingAllowed) {
    this.reorderingAllowed = reorderingAllowed;
    this.getTableHeader().setReorderingAllowed(reorderingAllowed);
  }


  /**
   * Set if it is allowed to resize columns.
   * @param reorderingAllowed flag used to set columns resizing
   */
  public final void setResizingAllowed(boolean resizingAllowed) {
    this.resizingAllowed = resizingAllowed;
    this.getTableHeader().setResizingAllowed(resizingAllowed);
  }


  /**
   * This method override the super class method, to fix a bug.
   * @param selRow selected row
   */
  public final void ensureRowIsVisible(int selRow) {
    java.awt.Rectangle r = getCellRect(this.getSelectedRow(),this.getSelectedColumn()==-1?0:this.getSelectedColumn(), false);
    scrollRectToVisible(r);
  }


  /**
   * This method override the super class mathod to add a vertical scrollbar listener and right mouse click event on the scroll bar...
   */
  protected void configureEnclosingScrollPane() {
    super.configureEnclosingScrollPane();
    Container p = getParent();
    if (p instanceof JViewport) {
        Container gp = p.getParent();
        if (gp instanceof JScrollPane) {
          scrollPane = (JScrollPane)gp;
          scrollPane.addMouseListener(new RightClickMouseListener());
          createVerticalScrollBarListener(scrollPane);
//          final Border defaultBorder = scrollPane.getBorder();

          Grid.this.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
              scrollPane.setBorder(BorderFactory.createLineBorder(ClientSettings.GRID_FOCUS_BORDER,2));
              Form.setCurrentFocusedForm(null);
            }

            public void focusLost(FocusEvent e) {
              scrollPane.setBorder(BorderFactory.createLineBorder(ClientSettings.GRID_NO_FOCUS_BORDER,2));
//              scrollPane.setBorder(defaultBorder);
            }
          });

        }
    }
  }




  /**
   * <p>Title: Open Swing</p>
   * <p>Description: Inner class used to listen right click mouse events, to view the grids.getPopup() menu.</p>
   * <p>Copyright: Copyright (c) 2006 Mauro Carniel</p>
   * @author Mauro Carniel
   * @version 1.0
   */
  class RightClickMouseListener extends MouseAdapter {

    public void mouseClicked(MouseEvent e) {
      if (e.getClickCount()==1 &&
          SwingUtilities.isRightMouseButton(e) &&
//          getSelectedColumn()!=-1 &&
          model.getMode()==Consts.READONLY) {

        if (Grid.this.columnAtPoint(e.getPoint())>=0)
          Grid.this.setColumnSelectionInterval(
              Grid.this.columnAtPoint(e.getPoint()),
              Grid.this.columnAtPoint(e.getPoint())
          );
        if (model.getRowCount()>0 && Grid.this.rowAtPoint(e.getPoint())>=0)
          Grid.this.setRowSelectionInterval(
              Grid.this.rowAtPoint(e.getPoint()),
              Grid.this.rowAtPoint(e.getPoint())
          );
//        if (Grid.this.rowAtPoint(e.getPoint())>=0)
          showPopupMenu(e.getX(),e.getY());
      }
    }
  }


  /**
   * Method called by quick filter panel to filter the specified column.
   * @param colProps column properties
   * @param value1 value used in the filter column (as unique filtering value or as the miminum value in a range filtering)
   * @param value2 (optional) value used in the filter column (as the maximum value in a range filtering)
   */
  public void filter(Column colProps,Object value1,Object value2) {
    if(colProps==null) return;

    FilterWhereClause[] values = null;

    if (value2!=null)
      grids.getQuickFilterValues().put(
          colProps.getColumnName(),
          values = new FilterWhereClause[] {
            new FilterWhereClause(colProps.getColumnName(),">=",value1),
            new FilterWhereClause(colProps.getColumnName(),"<=",value2)
          }
      );
    else {
       int colType=colProps.getColumnType();
       if ((colType==Column.TYPE_TEXT ||
            colType==Column.TYPE_LOOKUP) &&
            value1!=null && (value1.toString().endsWith("%") || value1.toString().startsWith("%")))
        grids.getQuickFilterValues().put(
            colProps.getColumnName(),
            values = new FilterWhereClause[] {
              new FilterWhereClause(colProps.getColumnName(),"like",value1),
              null
            }
        );
      else
        grids.getQuickFilterValues().put(colProps.getColumnName(),values = new FilterWhereClause[] {
          new FilterWhereClause(colProps.getColumnName(),"=",value1),
          null
        });

    }

    // set to a lower border the filterd column...
    int gridIndex = -1;
    for(int i=0;i<getModel().getColumnCount();i++) {
      try {
        gridIndex = this.convertColumnIndexToView(i);
        if (gridIndex!=-1) {
          if (colProps.getColumnName().equals(getVOListTableModel().getColumnName(gridIndex)))
            ((TableColumnHeaderRenderer)this.getTableHeader().getColumnModel().getColumn(gridIndex).getHeaderRenderer()).setBorder(BorderFactory.createLoweredBevelBorder());
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }


    grids.reload();

    grids.getRemovefilterItem().setVisible(true);
    grids.getPopup().setVisible(false);
    grids.getPopup().setVisible(true);
    grids.getFilterPanel().requestFocus();

  }


  /**
   * Remove filter from the specified column.
   * @param attributeName attribute name that identifies the column
   */
  public void removeQuickFilter(String attributeName) {
    // remove filter from the specified column...
    FilterWhereClause[] values = (FilterWhereClause[])grids.getQuickFilterValues().remove(attributeName);
    if (values==null)
      return;

    // set to a lower border the filterd column...
    int gridIndex = -1;
    for(int i=0;i<getModel().getColumnCount();i++) {
      try {
        gridIndex = this.convertColumnIndexToView(i);
        if (gridIndex!=-1) {
          if (attributeName.equals(getVOListTableModel().getColumnName(gridIndex)))
            ((TableColumnHeaderRenderer)this.getTableHeader().getColumnModel().getColumn(gridIndex).getHeaderRenderer()).setBorder(BorderFactory.createRaisedBevelBorder());
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    grids.reload();
  }


  /**
  * Method called by JVM when viewing this class.
  */
  public final void addNotify() {
    super.addNotify();
    if (firstTime) {
      // this flag allows only one execution of the following instructions...
      firstTime = false;

      dropTarget = new DropTarget(scrollPane, this);
      dropTarget2 = new DropTarget(this, this);

      // set descriptions into the column headers...
      for(int i=0;i<tableColumnModel.length;i++) {
        tableColumnModel[i].setHeaderValue(ClientSettings.getInstance().getResources().getResource(colProps[i].getHeaderColumnName()));
      }

    }
  }


  /**
   * @return <code>true</code> if TableModel is changed, <code>false</code> otherwise
   */
  public boolean isDataModified() {
    boolean modified = false;
    if (getMode()==Consts.INSERT)
      modified = true;
    else if (getMode()==Consts.EDIT) {
      if (stopCellEditing())
        modified = true;
      else
        modified = (getVOListTableModel().getChangedRows().size() > 0);
    }
    return(modified);
  }


  /**
   * @return <code>true</code> if current editing cell is in valid state, <code>false</code> otherwise
   */
  public final boolean stopCellEditing() {
    int row = getEditingRow();
    int col = getEditingColumn();

    if ((row>=0) && (col>=0))
      return(getCellEditor(row, col).stopCellEditing());
    else
      return true;
  }


  /**
   * Add a command to the grids.getPopup() menu.
   * @param item command to add in the grids.getPopup() menu
   */
  public final void addPopupCommand(JMenuItem item) {
    grids.getPopupCommands().add(item);
  }



  /**
   * @param columnName column name
   * @return int column index of the specified column name
   */
  public int getColumnIndex(String columnName) {
    return(this.modelAdapter.getFieldIndex(columnName));
  }


  /**
   * Set column header font in the specified column.
   */
  public void setHeaderTextFont(int columnIndex, Font font) {
    try {
      ((TableColumnHeaderRenderer) tableColumnModel[columnIndex].getHeaderRenderer()).setHeaderTextFont(font);
    }
    catch (Exception ex) {
      Logger.error(this.getClass().getName(), "setHeaderTextFont", "Error on setting font to the column header.", ex);
    }
  }


  public Font getHeaderTextFont(int columnIndex) {
    return(((TableColumnHeaderRenderer) tableColumnModel[columnIndex].getHeaderRenderer()).getHeaderTextFont());
  }


  /**
   * Set foreground color in the specified column header.
   */
  public void setHeaderTextForeground(int columnIndex, Color color) {
    try {
      ((TableColumnHeaderRenderer) tableColumnModel[columnIndex].getHeaderRenderer()).setHeaderTextForeground(color);
    }
    catch (Exception ex) {
      Logger.error(this.getClass().getName(), "setHeaderTextForeground", "Error on setting color to the column header.", ex);
    }
  }


  public Color getHeaderTextForeground(int columnIndex) {
    return(((TableColumnHeaderRenderer) tableColumnModel[columnIndex].getHeaderRenderer()).getHeaderTextForeground());
  }


  /**
   * @param rowHeightFixed define if row height can change for each row, according to image height included in a cell of grid; default value: <code>true</code>
   */
  public final void setRowHeightFixed(boolean rowHeightFixed) {
    this.rowHeightFixed = rowHeightFixed;
  }


  /**
   * @return define if row height can change for each row, according to image height included in a cell of grid; default value: <code>true</code>
   */
  public final boolean isRowHeightFixed() {
    return rowHeightFixed;
  }











  /********************************************************************
   *
   *             DRAG 'N DROP MANAGEMENTS METHODS
   *
   ********************************************************************/



  /************************************************************
   * DRAG MANAGEMENT
   ************************************************************/



  /**
   * Enable drag onto the grid.
   * @param gridId grid identifier
   */
  public final void enableDrag(String gridId) {
    try {
      this.gridId = gridId;
      dragSource.createDefaultDragGestureRecognizer(
          this,
          DnDConstants.ACTION_MOVE,
          new DragGestureAdapter(this)
      );
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  class DragGestureAdapter implements DragGestureListener {

    private DragSourceListener dragListener = null;

    public DragGestureAdapter(DragSourceListener dragListener) {
      this.dragListener = dragListener;
    }


    /**
     * A drag gesture has been initiated.
     */
    public final void dragGestureRecognized( DragGestureEvent event) {
      if (gridController.dragEnabled()) {
        int[] selIndexes = Grid.this.getSelectedRows();
        if ( selIndexes.length>0 ){
          try {
            MDIFrame.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          }
          catch (Exception ex) {
          }
          dragSource.startDrag (event, DragSource.DefaultMoveDrop, new StringSelection(gridId), dragListener);
        }
      }
      else
        // drag interrupted...
        event.getSourceAsDragGestureRecognizer().resetRecognizer();
    }

  }


  /**
   * This message goes to DragSourceListener, informing it that the dragging has entered the DropSite
   */
  public final void dragEnter (DragSourceDragEvent event) {
    gridController.dragEnter();
  }


  /**
   * This message goes to DragSourceListener, informing it that the dragging has exited the DropSite.
   */
  public final void dragExit (DragSourceEvent event) {
    gridController.dragExit();
  }


  /**
   * This message goes to DragSourceListener, informing it that the dragging is currently ocurring over the DropSite.
   */
  public final void dragOver (DragSourceDragEvent event) {
    gridController.dragOver();
  }


  /**
   * This method is invoked when the user changes the dropAction.
   */
  public final void dropActionChanged ( DragSourceDragEvent event) { }


  /**
   * This message goes to DragSourceListener, informing it that the dragging has ended.
   */
  public final void dragDropEnd (DragSourceDropEvent event) {
    gridController.dragDropEnd();
  }



  /************************************************************
   * DROP MANAGEMENT
   ************************************************************/

  /**
   * This method is invoked when you are dragging over the DropSite.
   */
  public final void dragEnter (DropTargetDragEvent event) {
    event.acceptDrag (DnDConstants.ACTION_MOVE);
    gridController.dropEnter();
  }


  /**
   * This method is invoked when you are exit the DropSite without dropping.
   */
  public final void dragExit (DropTargetEvent event) {
    try {
      gridController.dropExit();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

  }

  /**
   * This method is invoked when a drag operation is going on.
   */
  public final void dragOver (DropTargetDragEvent event) {
    int row = rowAtPoint(event.getLocation());
    gridController.dropOver(row);
  }


  /**
   * This method is invoked when a drop event has occurred.
   */
  public final void drop(DropTargetDropEvent event) {
    try {
      Transferable transferable = event.getTransferable();
      if (transferable.isDataFlavorSupported (DataFlavor.stringFlavor)){

        if (gridController.dropEnabled((String)transferable.getTransferData ( DataFlavor.stringFlavor))) {
          event.acceptDrop(DnDConstants.ACTION_MOVE);
          event.getDropTargetContext().dropComplete(true);
        }
        else
          event.rejectDrop();

      } else{
        event.rejectDrop();
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
      event.rejectDrop();
    } finally {
      try {
        MDIFrame.getInstance().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
      catch (Exception ex) {
      }

    }
  }

  /**
   * This method is invoked if the use modifies the current drop gesture.
   */
  public final void dropActionChanged ( DropTargetDragEvent event ) {
    gridController.dropActionChanged();
  }


  public void columnMarginChanged(ChangeEvent e) {
    super.columnMarginChanged(e);
    try {
      // ricalcolo l'ampiezza del pannello "tableHeader", a seguito del ridimensionamento di una colonna...
      int width = 0;
      for(int i=0;i<tableHeader.getColumnModel().getColumnCount();i++)
        width += tableHeader.getColumnModel().getColumn(i).getWidth();
      tableHeader.setPreferredSize(new Dimension(width,ClientSettings.HEADER_HEIGHT));

    }
    catch (Exception ex) {
    }
  }


}
