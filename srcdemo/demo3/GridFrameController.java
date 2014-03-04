package demo3;

import org.openswing.swing.table.client.GridController;
import java.util.*;
import org.openswing.swing.message.receive.java.*;
import java.sql.*;
import org.openswing.swing.message.send.java.FilterWhereClause;
import org.openswing.swing.table.java.GridDataLocator;
import java.awt.Font;
import org.openswing.swing.client.GridControl;
import java.math.BigDecimal;
import org.openswing.swing.util.java.Consts;
import org.openswing.swing.server.QueryUtil;
import org.openswing.swing.message.send.java.GridParams;


/**
 * <p>Title: OpenSwing Framework</p>
 * <p>Description: Grid controller.</p>
 * <p>Copyright: Copyright (C) 2006 Mauro Carniel</p>
 * <p> </p>
 * @author Mauro Carniel
 * @version 1.0
 */
public class GridFrameController extends GridController implements GridDataLocator {

  private GridFrame grid = null;
  private Connection conn = null;

  public GridFrameController(Connection conn) {
    this.conn = conn;
    grid = new GridFrame(conn,this);
  }


  /**
   * Callback method invoked to load data on the grid.
   * @param action fetching versus: PREVIOUS_BLOCK_ACTION, NEXT_BLOCK_ACTION or LAST_BLOCK_ACTION
   * @param startPos start position of data fetching in result set
   * @param filteredColumns filtered columns
   * @param currentSortedColumns sorted columns
   * @param currentSortedVersusColumns ordering versus of sorted columns
   * @param valueObjectType v.o. type
   * @param otherGridParams other grid parameters
   * @return response from the server: an object of type VOListResponse if data loading was successfully completed, or an ErrorResponse onject if some error occours
   */
  public Response loadData(
      int action,
      int startIndex,
      Map filteredColumns,
      ArrayList currentSortedColumns,
      ArrayList currentSortedVersusColumns,
      Class valueObjectType,
      Map otherGridParams) {
    PreparedStatement stmt = null;
    try {
      String sql = "select DEMO3.TEXT,DEMO3.DECNUM,DEMO3.CURRNUM,DEMO3.THEDATE,DEMO3.COMBO,DEMO3.CHECK_BOX,DEMO3.RADIO,DEMO3.CODE,DEMO3_LOOKUP.DESCRCODE,DEMO3.FORMATTED_TEXT,DEMO3.INT_VALUE,DEMO3.ML_TEXT,DEMO3.YEAR from DEMO3,DEMO3_LOOKUP where DEMO3.CODE=DEMO3_LOOKUP.CODE";
      ArrayList vals = new ArrayList();

/*
      Vector vals = new Vector();
      if (filteredColumns.size()>0) {
        FilterWhereClause[] filter = (FilterWhereClause[])filteredColumns.get("dateValue");
        if (filter!=null) {
          if (filter[0].getValue()!=null) {
            sql += " and DEMO3.THEDATE "+ filter[0].getOperator()+"?";
            vals.add(new java.sql.Date(((java.util.Date)filter[0].getValue()).getTime()));
          }
          else
            sql += " and DEMO3.THEDATE "+ filter[0].getOperator();

          if (filter[1]!=null) {
            if (filter[1].getValue()!=null) {
              sql += " and DEMO3.THEDATE "+ filter[1].getOperator()+"?";
              vals.add(new java.sql.Date(((java.util.Date)filter[1].getValue()).getTime()));
            }
            else
              sql += " and DEMO3.THEDATE "+ filter[1].getOperator();
          }
        }
        filter = (FilterWhereClause[])filteredColumns.get("comboValue");
        if (filter!=null) {
          if (filter[0].getValue()!=null) {
            if (filter[0].getOperator().equals(Consts.IN)) {
              sql += " and DEMO3.COMBO "+ filter[0].getOperator()+"(";
              ArrayList values = (ArrayList)filter[0].getValue();
              for(int j=0;j<values.size();j++) {
                sql += "?,";
                vals.add(values.get(j));
              }
              sql = sql.substring(0,sql.length()-1)+")";
            }
            else {
              sql += " and DEMO3.COMBO "+ filter[0].getOperator()+" ?";
              vals.add(filter[0].getValue());
            }
          }
          else
            sql += " and DEMO3.COMBO "+ filter[0].getOperator();

          if (filter[1]!=null) {
            if (filter[1].getValue()!=null) {
              sql += " and DEMO3.COMBO "+ filter[1].getOperator()+"?";
              vals.add(filter[1]);
            }
            else
              sql += " and DEMO3.COMBO "+ filter[1].getOperator();
          }
        }
      }
      if (currentSortedColumns.size()>0) {
        sql += " ORDER BY DEMO3.TEXT "+currentSortedVersusColumns.get(0);
      }

      stmt = conn.prepareStatement(sql);
      for(int i=0;i<vals.size();i++)
        stmt.setObject(i+1,vals.get(i));

      ResultSet rset = stmt.executeQuery();


      ArrayList list = new ArrayList();
      TestVO vo = null;
      while (rset.next()) {
        vo = new TestVO();
        vo.setCheckValue(rset.getObject(6)==null || !rset.getObject(6).equals("Y") ? Boolean.FALSE:Boolean.TRUE);
        vo.setComboValue(rset.getBigDecimal(5)==null?null:new Integer(rset.getBigDecimal(5).intValue()));
        vo.setCurrencyValue(rset.getBigDecimal(3));
        vo.setDateValue(rset.getDate(4));
        vo.setNumericValue(rset.getBigDecimal(2));
        vo.setRadioButtonValue(rset.getObject(7)==null || !rset.getObject(7).equals("Y") ? Boolean.FALSE:Boolean.TRUE);
        vo.setStringValue(rset.getString(1));
        vo.setLookupValue(rset.getString(8));
        vo.setDescrLookupValue(rset.getString(9));
        vo.setFormattedTextValue(rset.getBigDecimal(10));
        vo.setIntValue(rset.getBigDecimal(11)==null?null:new Integer(rset.getBigDecimal(11).intValue()));
        vo.setMultiLineTextValue(rset.getString(12));
        list.add(vo);
      }
      return new VOListResponse(list,false,list.size());

*/

      HashMap map = new HashMap();
      map.put("stringValue","DEMO3.TEXT");
      map.put("numericValue","DEMO3.DECNUM");
      map.put("currencyValue","DEMO3.CURRNUM");
      map.put("dateValue","DEMO3.THEDATE");
      map.put("comboValue","DEMO3.COMBO");
      map.put("checkValue","DEMO3.CHECK_BOX");
      map.put("radioButtonValue","DEMO3.RADIO");
      map.put("lookupValue","DEMO3.CODE");
      map.put("descrLookupValue","DEMO3_LOOKUP.DESCRCODE");
      map.put("formattedTextValue","DEMO3.FORMATTED_TEXT");
      map.put("intValue","DEMO3.INT_VALUE");
      map.put("multiLineTextValue","DEMO3.ML_TEXT");
      map.put("year","DEMO3.YEAR");
      return QueryUtil.getQuery(
        conn,
        sql,
        vals,
        map,
        TestVO.class,
        "Y",
        "N",
        new GridParams(
          action,
          startIndex,
          filteredColumns,
          currentSortedColumns,
          currentSortedVersusColumns,
          new HashMap()
        ),
        true
      );

    }
    catch (Exception ex) {
      ex.printStackTrace();
      return new ErrorResponse(ex.getMessage());
    }
    finally {
      try {
        if(stmt!=null)
          stmt.close();
      }
      catch (Exception ex1) {
      }
    }

  }


  /**
   * Method invoked when the user has clicked on save button and the grid is in INSERT mode.
   * @param rowNumbers row indexes related to the new rows to save
   * @param newValueObjects list of new value objects to save
   * @return an ErrorResponse value object in case of errors, VOListResponse if the operation is successfully completed
   */
  public Response insertRecords(int[] rowNumbers, ArrayList newValueObjects) throws Exception {

    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement("insert into DEMO3(TEXT,DECNUM,CURRNUM,THEDATE,COMBO,CHECK_BOX,RADIO,CODE,FORMATTED_TEXT,INT_VALUE,ML_TEXT,YEAR) values(?,?,?,?,?,?,?,?,?,?,?,?)");
      TestVO vo = (TestVO)newValueObjects.get(0);
      stmt.setObject(6,vo.getCheckValue());
//      stmt.setObject(6,vo.getCheckValue()==null || !vo.getCheckValue().booleanValue() ? "N":"Y");
      stmt.setBigDecimal(5,vo.getComboValue()==null?null:new BigDecimal(vo.getComboValue().intValue()));
      stmt.setBigDecimal(3,vo.getCurrencyValue());
      stmt.setDate(4,vo.getDateValue());
      stmt.setFloat(2,vo.getNumericValue().floatValue());
      stmt.setObject(7,vo.getRadioButtonValue()==null || !vo.getRadioButtonValue().booleanValue() ? "N":"Y");
      stmt.setString(1,vo.getStringValue());
      stmt.setString(8,vo.getLookupValue());
      stmt.setBigDecimal(9,vo.getFormattedTextValue());
      stmt.setBigDecimal(10,vo.getIntValue()==null?null:new BigDecimal(vo.getIntValue().intValue()));
      stmt.setString(11,vo.getMultiLineTextValue());
      stmt.setInt(12,vo.getYear().intValue());
      stmt.execute();
      return new VOListResponse(newValueObjects,false,newValueObjects.size());
    }
    catch (SQLException ex) {
      ex.printStackTrace();
      return new ErrorResponse(ex.getMessage());
    }
    finally {
      try {
        stmt.close();
        conn.commit();
      }
      catch (SQLException ex1) {
      }
    }

  }


  /**
   * Method invoked when the user has clicked on save button and the grid is in EDIT mode.
   * @param rowNumbers row indexes related to the changed rows
   * @param oldPersistentObjects old value objects, previous the changes
   * @param persistentObjects value objects relatied to the changed rows
   * @return an ErrorResponse value object in case of errors, VOListResponse if the operation is successfully completed
   */
  public Response updateRecords(int[] rowNumbers,ArrayList oldPersistentObjects,ArrayList persistentObjects) throws Exception {
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement("update DEMO3 set TEXT=?,DECNUM=?,CURRNUM=?,THEDATE=?,COMBO=?,CHECK_BOX=?,RADIO=?,CODE=?,FORMATTED_TEXT=?,INT_VALUE=?,ML_TEXT=?,YEAR=? where TEXT=?");
      TestVO vo = null;
      for(int i=0;i<persistentObjects.size();i++) {
        vo = (TestVO)persistentObjects.get(i);
        stmt.setObject(6,vo.getCheckValue());
//        stmt.setObject(6,vo.getCheckValue()==null || !vo.getCheckValue().booleanValue() ? "N":"Y");
        stmt.setBigDecimal(5,vo.getComboValue()==null?null:new BigDecimal(vo.getComboValue().intValue()));
        stmt.setBigDecimal(3,vo.getCurrencyValue());
        stmt.setDate(4,vo.getDateValue());
        stmt.setFloat(2,vo.getNumericValue().floatValue());
        stmt.setObject(7,vo.getRadioButtonValue()==null || !vo.getRadioButtonValue().booleanValue() ? "N":"Y");
        stmt.setString(1,vo.getStringValue());
        stmt.setString(8,vo.getLookupValue());
        stmt.setBigDecimal(9,vo.getFormattedTextValue());
        stmt.setBigDecimal(10,vo.getIntValue()==null?null:new BigDecimal(vo.getIntValue().intValue()));
        stmt.setString(11,vo.getMultiLineTextValue());
        stmt.setInt(12,vo.getYear().intValue());
        stmt.setString(13,vo.getStringValue());
        stmt.execute();
      }
      return new VOListResponse(persistentObjects,false,persistentObjects.size());
    }
    catch (SQLException ex) {
      ex.printStackTrace();
      return new ErrorResponse(ex.getMessage());
    }
    finally {
      try {
        stmt.close();
        conn.commit();
      }
      catch (SQLException ex1) {
      }
    }

  }


  /**
   * Method invoked when the user has clicked on delete button and the grid is in READONLY mode.
   * @param persistentObjects value objects to delete (related to the currently selected rows)
   * @return an ErrorResponse value object in case of errors, VOResponse if the operation is successfully completed
   */
  public Response deleteRecords(ArrayList persistentObjects) throws Exception {
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement("delete from DEMO3 where TEXT=?");
      for(int i=0;i<persistentObjects.size();i++) {
        TestVO vo = (TestVO)persistentObjects.get(i);
        stmt.setString(1,vo.getStringValue());
        stmt.execute();
      }
      return new VOResponse(new Boolean(true));
    }
    catch (SQLException ex) {
      ex.printStackTrace();
      return new ErrorResponse(ex.getMessage());
    }
    finally {
      try {
        stmt.close();
        conn.commit();
      }
      catch (SQLException ex1) {
      }
    }

  }


  /**
   * Callback method invoked each time a cell is edited: this method define if the new value if valid.
   * This method is invoked ONLY if:
   * - the edited value is not equals to the old value OR it has exmplicitely called setCellAt or setValueAt
   * - the cell is editable
   * Default behaviour: cell value is valid.
   * @param rowNumber selected row index
   * @param attributeName attribute name related to the column currently selected
   * @param oldValue old cell value (before cell editing)
   * @param newValue new cell value (just edited)
   * @return <code>true</code> if cell value is valid, <code>false</code> otherwise
   */
  public boolean validateCell(int rowNumber,String attributeName,Object oldValue,Object newValue) {
    // this method has been overrrided to listen for numeric/currency cell changes:
    // in this case it will be invoked getTotals method to refresh bottom grid content...
    if (attributeName.equals("currencyValue") || attributeName.equals("numericValue")) {
      grid.getGrid().getBottomTable().reload();
    }
    if (attributeName.equals("intValue") && new Integer(0).equals(newValue)) {
      // zero value not allowed...
      return false;
    }
    return true;
  }


  /**
   * Callback method invoked when the user has selected another row.
   * @param rowNumber selected row index
   */
  public void rowChanged(int rowNumber) {
  }



  /**
   * @param grid grid
   * @param row selected row index
   * @param attributeName attribute name that identifies the selected grid column
   * @return <code>true</code> if the selected cell is editable, <code>false</code> otherwise
   */
  public boolean isCellEditable(GridControl grid,int row,String attributeName) {
    TestVO vo = (TestVO)grid.getVOListTableModel().getObjectForRow(row);
    if ("formattedTextValue".equals(attributeName))
      return vo.getCheckValue()!=null && vo.getCheckValue().equals("Y");
//      return vo.getCheckValue()!=null && vo.getCheckValue().booleanValue();
    return grid.isFieldEditable(row,attributeName);
  }

  /**
   * Callback method invoked when a grid cell is selected.
   * @param rowNumber selected row index
   * @param columnIndex column index related to the column currently selected
   * @param attributedName attribute name related to the column currently selected
   * @param persistentObject v.o. related to the selected row
   */
  public void selectedCell(int rowNumber, int columnIndex, String attributedName, ValueObject persistentObject) {
    System.out.println("Selected cell: ("+rowNumber+","+columnIndex+")");
  }


  public Font getFont(int row,String attributeName,Object value,Font defaultFont) {
      return new Font(defaultFont.getFontName(),Font.BOLD,defaultFont.getSize());
  }



}
