package org.codesaurus.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * <h1>StringTable</h1>
 * <p>
 * StringTable allows developers to quickly visualise tabulated data in user interfaces or logs
 * by providing simple API to construct and output the data.
 *
 * Please see {@link org.codesaurus.utils.StringTableWriters} for available output formats
 * </p>
 *
 * <pre>
 *      StringTable st = new StringTable("First Column", "Second Column", "Third Column");
 *      for (int i = 0; i < 10; i++) {
 *          st.addRow(i, i, i);
 *      }
 *      System.out.println(StringTableWriters.writeStringTableAsCSV(st));
 *      System.out.println(StringTableWriters.writeStringTableAsASCII(st));
 *      System.out.println(StringTableWriters.writeStringTableAsHtml(st, false, 2));
 * </pre>
 *
 * @author Simon Johnson <simon622 AT gmail.com>
 * @since 01/2004 - initial implementation
 *
 * 2017 - rollup and row index support
 * 2019 - table sort added
 *      matt h - fixed double numerical sort
 */
public class StringTable {

    private boolean rowIndexAdded = false;
    private String tableName;
    private List<String> headings;
    private List<String> footer;
    private List<String[]> rows = new ArrayList<String[]>();
    private Map<Integer, Comparator<String>> COLUMN_SORTERS = new HashMap<>();

    private static final Comparator<String> DEFAULT_SORT = new Comparator<String>() {
        public int compare(String o1, String o2) {
            String val1 = o1.trim();
            String val2 = o2.trim();
            try {
                double d1 = Double.parseDouble(val1);
                double d2 = Double.parseDouble(val2);
                return Double.compare(d1, d2);
            } catch (Exception e) {
            }
            return String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
        }
    };

    public StringTable() {
    }

    public StringTable(String...headings){
        this(null, headings);
    }

    public StringTable(String tableName, String[] headings){

        this.tableName = tableName;
        this.headings = new ArrayList<String>(headings.length);
        this.headings.addAll(Arrays.asList(headings));
    }

    public List<String> getHeadings() {
        return headings;
    }

    public void setHeadings(List<String> headings) {
        this.headings = headings;
    }

    public void setTableName(String tableName){
        this.tableName = tableName;
    }

    public String getTableName(){
        return this.tableName;
    }

    public void setRows(List<String[]> rows){
        this.rows = rows;
    }

    public List<String[]> getRows(){
        return rows;
    }

    public List<String> getFooter(){
        return footer;
    }

    public void showRowIndex(){
        rowIndexAdded = true;
    }

    public void hideRowIndex(){
        rowIndexAdded = false;
    }

    public synchronized void sort(final int colIdx, boolean reverse) {
        Comparator<String[]> wrapper = new Comparator<String[]>() {
            public int compare(String[] o1, String[] o2) {
                if(o1.length >= (colIdx + 1) && o2.length >= (colIdx + 1)){
                    return getComparatorForColumn(colIdx).compare(o1[colIdx].trim(), o2[colIdx].trim());
                }
                return 0;
            }
        };
        rows.sort(reverse ? wrapper.reversed() : wrapper);
    }

    public void addColumnComparator(int colIdx, Comparator<String> c){
        COLUMN_SORTERS.put(colIdx, c);
    }

    private Comparator<String> getComparatorForColumn(int colIdx){
        Comparator<String> c = COLUMN_SORTERS.get(colIdx);
        if(c == null) {
            c = DEFAULT_SORT;
        }
        return c;
    }

    public synchronized void addFooter(String... row){
        if(row == null || row.length == 0) return;
        this.footer = new ArrayList<String>(row.length);
        this.footer.addAll(Arrays.asList(row));
    }

    public synchronized void addRow(Object... row){

        if(row == null || row.length == 0) return;
        String[] a = new String[row.length];
        for (int i = 0; i < row.length; i++) {
            a[i] = String.valueOf(row[i]);
        }
        rows.add(a);
    }

    public void addRows(List<String[]> rows){

        Iterator<String[]> itr = rows.iterator();
        while(itr.hasNext()){
            String[] row = itr.next();
            addRow((Object[])row);
        }
    }

    public void combine(StringTable... tables){
        if(tables != null){
            for (StringTable stringTable : tables) {
                if(StringTable.getColumnCount(stringTable) ==
                        StringTable.getColumnCount(this)){
                    addRows(stringTable.getRows());
                }
            }
        }
    }

    public synchronized void addCellValue(Object s){
        String[] arr = rows.size() == 0 ? null : rows.get(rows.size() - 1);
        if(arr == null || arr.length >=  getHeadings().size()){
            arr = nextRow();
        }
        String[] newarr = new String[arr.length+1];
        System.arraycopy(arr, 0, newarr, 0, arr.length);
        newarr[arr.length] = s == null ? "" : String.valueOf(s);
        rows.set(rows.size() - 1, newarr);
    }

    private String[] nextRow(){
        String[] arr = new String[0];
        rows.add(arr);
        return arr;
    }

    public void addSeparator(){
        rows.add(null);
    }

    public static String getColumnName(StringTable table, int idx){
        if(table.rowIndexAdded) {
            if(idx == 0) return "Row";
            idx = idx - 1;
        }
        return table.getHeadings().get(idx);
    }

    public static int getColumnCount(StringTable table){
        int count = table.getHeadings().size();
        if(table.rowIndexAdded) count++;
        return count;
    }

    public static int size(StringTable table){
        return table.getRows().size();
    }

    public String getCellValue(int rowIdx, int colIdx){
        if(rowIndexAdded) {
            if(colIdx == 0) return "" + (rowIdx + 1);
            colIdx = colIdx - 1;
        }
        String val = rows.get(rowIdx)[colIdx];
        return val == null ? "" : val;
    }

    public <T> void addRows(Collection<T> collection, Function<? super T, Object[]> mapper) {
        if (null != collection && null != mapper) {
            collection.stream().forEach(r -> addRow(mapper.apply(r)));
        }
    }
}