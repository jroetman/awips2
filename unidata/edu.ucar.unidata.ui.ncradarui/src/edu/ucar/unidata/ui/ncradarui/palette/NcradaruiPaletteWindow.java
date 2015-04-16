/**
 * 
 * edu.ucar.unidata.ui.ncradarui.palette.ncradarPaletteWindow
 * 
 * This java class performs the NCRADAR GUI construction.
 * This code has been developed by the SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    	Engineer    Description
 * -------		------- 	-------- 	-----------
 * 12/24/2009		TBD		Chin Chen	Initial coding
 * 06/28/2011       T402       X. Guo   Re-format NCRADAR view panel, check
 *                                      the click action on ncradar legend
 * 02/15/2012     #972      G. Hull     NatlCntrsEditor 
 *
 * </pre>
 * 
 * @author Chin Chen
 * @version 1.0
 */
package edu.ucar.unidata.ui.ncradarui.palette;

import edu.ucar.unidata.ui.ncradarui.dbutil.EReportTimeRange;
import edu.ucar.unidata.ui.ncradarui.dbutil.NcradarDbQuery;
import edu.ucar.unidata.ui.ncradarui.dbutil.NcradarStationInfo;
import edu.ucar.unidata.ui.ncradarui.rsc.NcradaruiResource;
import gov.noaa.nws.ncep.viz.ui.display.NatlCntrsEditor;
import gov.noaa.nws.ncep.viz.ui.display.NcDisplayMngr;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.viz.ui.UiUtil;

public class NcradaruiPaletteWindow extends ViewPart implements
        SelectionListener, DisposeListener, IPartListener {

    private String selectedGp = null;

    private String selectedType = null;
	
	private IWorkbenchPage page;
	
	private org.eclipse.swt.widgets.List gpWdgList = null;

	private org.eclipse.swt.widgets.List typeWdgList = null;

	private NcradarDbQuery query;

	private EReportTimeRange timeCovered = EReportTimeRange.TWELVE_HOURS;

	private boolean isState = false;

	private boolean replaceText = true;

	private String gpLabelStr = "Select Data Type Group ";

	private String productLabelStr = "Select Data Type Product";

	private Group dataTypeGp;

	private Group prodListGp;

	private Group timeGp;

	private Group staStnGp;

	private Group textGp;

	private String currentProductName = null;

	private int colWidth = 200;

	private int listHeight = 150;

	private int btnGapY = 5;

	private int btnGapX = 5;

	private int timeBtnWidth = 40;

	private int staBtnWidth = 75;

	private int btnHeight = 20;

	private int pushbtnHeight = 25;

	private int labelGap = 20;

	private static int ASCII_CR_VAL = 13;

	private Text text;

	private Button nextBtn, prevBtn;

	private java.util.List<NcradarStationInfo> points = new ArrayList<NcradarStationInfo>();

    // private static NCMapEditor mapEditor = null;
	private HandlePrinting printingHandle;
	
	private boolean isEditorVisible = true;

	private int dataTypeGpItem = 0;
	
    private int dataTypePdItem = 0;
	
    /*
     * public static NCMapEditor getMapEditor() { if(mapEditor== null)
     * createMapEditor(); return mapEditor; }
     */
    // handle Text Mode
	private java.util.List<Object[]> currentTextReports = null;

	private int currentTextIndex;
	
    public int getCurrentTextIndex() {
		return currentTextIndex;
	}

	public void setCurrentTextIndex(int currentTextIndex) {
		this.currentTextIndex = currentTextIndex;
	}

	public java.util.List<Object[]> getCurrentTextReports() {
		return currentTextReports;
	}

    public void setCurrentTextReports(
            java.util.List<Object[]> currentTextReports) {
		this.currentTextReports = currentTextReports;
	}
	
	public java.util.List<NcradarStationInfo> getPoints() {
		return points;
	}

	public void setPoints(java.util.List<NcradarStationInfo> points) {
		this.points = points;
	}
	
	public boolean isReplaceText() {
		return replaceText;
	}

	public void setReplaceText(boolean replaceText) {
		this.replaceText = replaceText;
	}
	
	public EReportTimeRange getTimeCovered() {
		return timeCovered;
	}

	public void setTimeCovered(EReportTimeRange timeCovered) {
		this.timeCovered = timeCovered;
	}

	public boolean isState() {
		return isState;
	}

	public void setState(boolean isState) {
		this.isState = isState;
	}

	public String getCurrentProductName() {
		return currentProductName;
	}

	public void setCurrentProductName(String currentProductName) {
		this.currentProductName = currentProductName;
	}

	public void enableNextBtn(boolean enable) {
		nextBtn.setEnabled(enable);
	}

	public void enablePrevBtn(boolean enable) {
		prevBtn.setEnabled(enable);
	}
	
    public void setEditorVisible(boolean isVisible) {
		this.isEditorVisible = isVisible;
	}

    public boolean getEditorVisible() {
		return this.isEditorVisible;
	}
	
    public void setDataTypeGroupItem(int item) {
		this.dataTypeGpItem = item;
	}
	
    public int getDataTypeGroupItem() {
		return this.dataTypeGpItem;
	}
	
    public void setDataTypeProductItem(int item) {
		this.dataTypePdItem = item;
	}
	
    public int getDataTypeProductItem() {
		return this.dataTypePdItem;
	}

	/*
     * private static void createMapEditor(){ // create an editor MapEditor
     * if(mapEditor != null) return; File rbdFile =
     * LocalizationManager.getInstance().getLocalizationFile("defaultRBDFile");
     * try { mapEditor =
     * NcDisplayMngr.createNatlCntrsEditor("NCRadar-Map","NCRADAR" ); NcMapRBD rbd
     * = NcMapRBD.unmarshalRBD( rbdFile, null ); rbd.setNcEditor( mapEditor );
     * ResourceBndlLoader rbdLoader = new ResourceBndlLoader("DefaultMap");
     * rbdLoader.addRBD( rbd ); VizApp.runSync( rbdLoader );
     * //System.out.println("NcradaruiPaletteWindow create editor "+
     * mapEditor.toString()); } catch ( Exception ve ) {
     * System.out.println("Could not load initial editor: " + ve.getMessage());
     * ve.printStackTrace(); } }
     */
	/**
	 * Constructor
	 * 
	 */
	public NcradaruiPaletteWindow() {

		super();
        if (ncradaruiPaletteWindow == null)
			ncradaruiPaletteWindow = this;
		else {
			NcradaruiPaletteWindow tmp = ncradaruiPaletteWindow;
			ncradaruiPaletteWindow = this;
            ncradaruiPaletteWindow.setCurrentProductName(tmp
                    .getCurrentProductName());
            ncradaruiPaletteWindow.setDataTypeGroupItem(tmp
                    .getDataTypeGroupItem());
            ncradaruiPaletteWindow.setDataTypeProductItem(tmp
                    .getDataTypeProductItem());
            ;
			ncradaruiPaletteWindow.selectedGp = tmp.selectedGp;
			ncradaruiPaletteWindow.selectedType = tmp.selectedType;
            ncradaruiPaletteWindow.setTimeCovered(tmp.getTimeCovered());
            ncradaruiPaletteWindow.setState(tmp.isState());
		}
        // System.out.println("ncradaruiPaletteWindow is created!!!!!!!!!!!!");
		
		// create an editor
        // createMapEditor();

	}

    // create this singleton object
	private static NcradaruiPaletteWindow ncradaruiPaletteWindow = null; 

	public static NcradaruiPaletteWindow getAccess() {
		return ncradaruiPaletteWindow;
	}

	/**
	 * Invoked by the workbench to initialize this View.
	 */
    public void init(IViewSite site) {
        // System.out.println("ncradaruiPaletteWindow inited!!");
		try {

            super.init(site);

        } catch (PartInitException pie) {

			pie.printStackTrace();

		}
		NcradaruiResource.registerMouseHandler();
        page = site.getPage();
        page.addPartListener(this);

	}

	/**
	 * Disposes resource.  invoked by the workbench
	 */
	public void dispose() {
        // System.out.println("NcradaruiPaletteWindow dispose me, isEditorVisible="+
        // isEditorVisible);
        if (!isEditorVisible) {
	    	NcradaruiResource.unregisterMouseHandler();
	    	return;
        } else {
	    	super.dispose();

	    	NatlCntrsEditor editor = NcradaruiResource.getMapEditor();
	    	
            if (editor != null) {
                for (IRenderableDisplay display : UiUtil
                        .getDisplaysFromContainer(editor)) {
                    // System.out.println("display " + display.toString());
                    for (ResourcePair rp : display.getDescriptor()
                            .getResourceList()) {
                        if (rp.getResource() instanceof NcradaruiResource) {
                            NcradaruiResource rsc = (NcradaruiResource) rp
                                    .getResource();
	    					rsc.unload();
	    				}
	    			}
	    		}
	    	}
	    	ncradaruiPaletteWindow = null;

	    	/*
	    	 * remove the workbench part listener
	    	 */
	    	page.removePartListener(this);
	    }

 	}
	
    private void close() {
        IWorkbenchPage wpage = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage();
		
        IViewPart vpart = wpage.findView("edu.ucar.unidata.ui.NCRADARUI");
        wpage.hideView(vpart);
        
        NcDisplayMngr.setPanningMode();
	}

	/**
     * Invoked by the workbench, this method sets up the SWT controls for the
     * ncradar palette
	 */
	@Override
    public void createPartControl(Composite parent) {

        parent.setLayout(new GridLayout(1, false));
        // create textGp group. It contains text and textMode group
        textGp = new Group(parent, SWT.SHADOW_OUT);
        textGp.setLayout(new GridLayout());
        textGp.setText("Text Report");
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        textGp.setLayoutData(data);

		createTextArea(textGp);
		createTextModeGp(textGp);		
			
        // Create ConfigGp group. It contains dataTypegp, dataProductGp, time
        // cover group and state/stn group
        Group configGp = new Group(parent, SWT.SHADOW_ETCHED_OUT);
        configGp.setLayout(new GridLayout(3, false));
 		
		createGpList(configGp);
		
        createProductList(configGp);
        
        createTimeCoverBtns(configGp);
        if (currentProductName != null)
            handleStnMarkingRequestByBtn();
        // createStaStnBtns(configGp);
        
 	}
	
	public void createGpList(Composite parent) {
        dataTypeGp = new Group(parent, SWT.SHADOW_ETCHED_IN);
		dataTypeGp.setText(gpLabelStr);
		
        // create GP widget list
        gpWdgList = new org.eclipse.swt.widgets.List(dataTypeGp, SWT.SINGLE
                | SWT.V_SCROLL);
        gpWdgList.setBounds(dataTypeGp.getBounds().x + btnGapX,
                dataTypeGp.getBounds().y + labelGap, colWidth, listHeight);
        // query gp list form ncradardbrsc
        query = NcradarDbQuery.getAccess();
        // query = ncradarDbQueryX.getAccess();
        java.util.List<String> groupList = query.getDataTypeGpList();
        
        if ((groupList != null) && (groupList.size() > 0)
                && (dataTypeGpItem < groupList.size()))
            selectedGp = groupList.get(dataTypeGpItem); /*
                                                         * set default select gp
                                                         * to first gp in the
                                                         * list
                                                         */
        else
        	return;
        // add gp item to gp widget list
        for (String str : groupList) {
            gpWdgList.add(str);
        }
        
        gpWdgList.setSelection(dataTypeGpItem);
              
        // create a selection listener to handle user's selection on gp list
        gpWdgList.addListener(SWT.Selection, new Listener() {
        	java.util.List<String> prodTypeList;

            public void handleEvent(Event e) {
                if (gpWdgList.getSelectionCount() > 0) {
    				selectedGp = gpWdgList.getSelection()[0];
                    // System.out.println("selected gp is " + selectedGp);
    				// query selected gp's product type list form ncradardbrsc
    				prodTypeList = query.getGpProductList(selectedGp);
    				typeWdgList.removeAll();
    				
                    for (String str : prodTypeList) {
                        // add product type list to widget list, note that
                        // Widgget list was created earlier. This part of code
                        // is
                        // listener event handler and is invoked when user picks
                        // gp
                        typeWdgList.add(str);
    			    }
                    typeWdgList.setSelection(0);
                    setDataTypeGroupItem(gpWdgList.getSelectionIndex());
    			}
    		}
    	});
		
	}

	public void createProductList(Composite parent) {
        prodListGp = new Group(parent, SWT.SHADOW_ETCHED_IN);
		prodListGp.setText(productLabelStr);      
        // create product type widget list
        typeWdgList = new org.eclipse.swt.widgets.List(prodListGp, SWT.SINGLE
                | SWT.V_SCROLL | SWT.H_SCROLL);
                
        typeWdgList.setBounds(prodListGp.getBounds().x + btnGapX,
                prodListGp.getBounds().y + labelGap, colWidth, listHeight);

        /* add default product type list */
        java.util.List<String> tempprodTypeList;
        tempprodTypeList = query.getGpProductList(selectedGp);
		typeWdgList.removeAll();
			
        if (tempprodTypeList == null || tempprodTypeList.size() == 0)
			return;
        for (String str : tempprodTypeList) {
            // add default product type list to widget list, note that Widgget
            // list was created earlier.
            typeWdgList.add(str);
		}
		    
        typeWdgList.setSelection(dataTypePdItem);
        typeWdgList.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                if (typeWdgList.getSelectionCount() > 0) {
    				selectedType = typeWdgList.getSelection()[0];
                    // System.out.println("selected type is " + selectedType);
    				currentProductName = selectedType;
                    // handle station marking here, when user picked product
                    // type
    				handleProductTypeStnMarking();
                    setDataTypeProductItem(typeWdgList.getSelectionIndex());
    			}
    		}
    	});		
	}

	private void handleProductTypeStnMarking() {
		points = query.getProductStaList(selectedType, timeCovered);

        if (points != null && points.size() == 1) {
			NcradaruiResource.getNcradaruiResource().setPoints(points);
			NcradaruiResource.getNcradaruiResource().setPickedStnPt(points);
            displayProduct(points.get(0));
        } else {

            if (points != null && points.size() > 1) {
				text.setText(" ");
            } else {
				text.setText("No Station Reports This Product At Selected Time Range");
			}
			nextBtn.setEnabled(false);
			prevBtn.setEnabled(false);
			NatlCntrsEditor mapEditor = NcradaruiResource.getMapEditor();
            if (mapEditor != null) {
				mapEditor.refresh();
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage().bringToTop(mapEditor);
			}

			NcradaruiResource.getNcradaruiResource().setPoints(points);
			NcradaruiResource.getNcradaruiResource().setPickedStnPt(null);
            // ncradaruiModalTool.setModal();
		}
	}
	
	private void handleStnMarkingRequestByBtn() {
		points = query.getProductStaList(selectedType, timeCovered);
        if (points != null && points.size() == 1) {
			NcradaruiResource.getNcradaruiResource().setPoints(points);
			NcradaruiResource.getNcradaruiResource().setPickedStnPt(points);
            displayProduct(points.get(0));
        } else {
            if ((points != null) && (points.size() != 0)) {

                if (replaceText)
					text.setText(" ");
            } else {
                if (replaceText)
					text.setText("No Report Available");
			}

			nextBtn.setEnabled(false);
			prevBtn.setEnabled(false);
			NatlCntrsEditor mapEditor = NcradaruiResource.getMapEditor();
            if (mapEditor != null) {
				mapEditor.refresh();
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage().bringToTop(mapEditor);
			}

			NcradaruiResource.getNcradaruiResource().setPoints(points);
            if (replaceText)
				NcradaruiResource.getNcradaruiResource().setPickedStnPt(null);

            // ncradaruiModalTool.setModal();
		}
	}

	public void createTimeCoverBtns(Composite parent) {
		
        Group composite = new Group(parent, SWT.NORMAL);
		composite.setLayout(new GridLayout(1, false));
		
        timeGp = new Group(composite, SWT.SHADOW_ETCHED_IN);
        timeGp.setText("Hour Covered");
        
        Listener btnListeener = new Listener() {
            public void handleEvent(Event event) {  
                String btnText = ((Button) (event.widget)).getText();
                if (btnText.equals("1"))
            		timeCovered = EReportTimeRange.ONE_HOUR; 
                else if (btnText.equals("3"))
            		timeCovered = EReportTimeRange.THREE_HOURS; 
                else if (btnText.equals("6"))
            		timeCovered = EReportTimeRange.SIX_HOURS; 
                else if (btnText.equals("12"))
            		timeCovered = EReportTimeRange.TWELVE_HOURS; 
                else if (btnText.equals("24"))
            		timeCovered = EReportTimeRange.TWENTYFOUR_HOURS; 
                else if (btnText.equals("48"))
            		timeCovered = EReportTimeRange.FORTYEIGHT_HOURS; 
            	else
            		timeCovered = EReportTimeRange.NONE;
            	handleStnMarkingRequestByBtn();
                System.out.println("button event # "
                        + ((Button) (event.widget)).getText());
            }        
            
        };
        
        Button oneHrBtn = new Button(timeGp, SWT.RADIO | SWT.BORDER);
        oneHrBtn.setText("1");
        oneHrBtn.setEnabled(true);
        oneHrBtn.setBounds(timeGp.getBounds().x + btnGapX, timeGp.getBounds().y
                + labelGap, timeBtnWidth, btnHeight);
        oneHrBtn.addListener(SWT.MouseUp, btnListeener);
        
        Button threeHrBtn = new Button(timeGp, SWT.RADIO);
        threeHrBtn.setText("3");
        threeHrBtn.setEnabled(true);
        threeHrBtn.setBounds(
                btnGapX + oneHrBtn.getBounds().x + oneHrBtn.getBounds().width,
                timeGp.getBounds().y + labelGap, timeBtnWidth, btnHeight);

        threeHrBtn.addListener(SWT.MouseUp, btnListeener);
        
        Button sixHrBtn = new Button(timeGp, SWT.RADIO);
        sixHrBtn.setText("6");
        sixHrBtn.setEnabled(true);
        sixHrBtn.setBounds(threeHrBtn.getBounds().x
                + threeHrBtn.getBounds().width + btnGapX, timeGp.getBounds().y
                + labelGap, timeBtnWidth, btnHeight);

        sixHrBtn.addListener(SWT.MouseUp, btnListeener);
        
        Button twelveHrBtn = new Button(timeGp, SWT.RADIO);
        twelveHrBtn.setText("12");
        twelveHrBtn.setEnabled(true);
        // twelveHrBtn.setSelection(true);//default selection
        twelveHrBtn.setBounds(timeGp.getBounds().x + btnGapX,
                oneHrBtn.getBounds().y + oneHrBtn.getBounds().height + btnGapY,
                timeBtnWidth, btnHeight);

        twelveHrBtn.addListener(SWT.MouseUp, btnListeener);
        
        Button twentyfourHrBtn = new Button(timeGp, SWT.RADIO);
        twentyfourHrBtn.setText("24");
        twentyfourHrBtn.setEnabled(true);
        twentyfourHrBtn.setBounds(btnGapX + twelveHrBtn.getBounds().x
                + twelveHrBtn.getBounds().width, threeHrBtn.getBounds().y
                + threeHrBtn.getBounds().height + btnGapY, timeBtnWidth,
                btnHeight);

        twentyfourHrBtn.addListener(SWT.MouseUp, btnListeener);
        
        Button fourtyeightHrBtn = new Button(timeGp, SWT.RADIO);
        fourtyeightHrBtn.setText("48");
        fourtyeightHrBtn.setEnabled(true);
        fourtyeightHrBtn.setBounds(btnGapX + twentyfourHrBtn.getBounds().x
                + twentyfourHrBtn.getBounds().width, sixHrBtn.getBounds().y
                + sixHrBtn.getBounds().height + btnGapY, timeBtnWidth,
                btnHeight);
 
        fourtyeightHrBtn.addListener(SWT.MouseUp, btnListeener);
        
        Button allHrBtn = new Button(timeGp, SWT.RADIO);
        allHrBtn.setText("all");
        allHrBtn.setEnabled(true);
        allHrBtn.setBounds(btnGapX + fourtyeightHrBtn.getBounds().x
                + fourtyeightHrBtn.getBounds().width, sixHrBtn.getBounds().y
                + sixHrBtn.getBounds().height + btnGapY, timeBtnWidth,
                btnHeight);
 
        if (timeCovered == EReportTimeRange.ONE_HOUR)
        	oneHrBtn.setSelection(true); 
    	else if (timeCovered == EReportTimeRange.THREE_HOURS)
    		threeHrBtn.setSelection(true); 
        else if (timeCovered == EReportTimeRange.SIX_HOURS)
    		sixHrBtn.setSelection(true); 
        else if (timeCovered == EReportTimeRange.TWELVE_HOURS)
    		twelveHrBtn.setSelection(true); 
        else if (timeCovered == EReportTimeRange.TWENTYFOUR_HOURS)
    		twentyfourHrBtn.setSelection(true); 
        else if (timeCovered == EReportTimeRange.FORTYEIGHT_HOURS)
    		fourtyeightHrBtn.setSelection(true); 
    	else
    		allHrBtn.setSelection(true);
        
        allHrBtn.addListener(SWT.MouseUp, btnListeener);
        createStaStnBtns(composite);
	}
	
	public void createStaStnBtns(Composite parent) {
        staStnGp = new Group(parent, SWT.LEFT);
        staStnGp.setText("Select By ");
        
        Button stationBtn = new Button(staStnGp, SWT.RADIO);
        stationBtn.setText("station");
        stationBtn.setEnabled(true);
        stationBtn.setBounds(staStnGp.getBounds().x + btnGapX,
                staStnGp.getBounds().y + labelGap, staBtnWidth, btnHeight);
        if (!isState)
            stationBtn.setSelection(true);

        stationBtn.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {           
            	isState = false;    
            	handleStnMarkingRequestByBtn();
            }          		            	 	
        });
        Button stateBtn = new Button(staStnGp, SWT.RADIO);
        stateBtn.setText("state");
        stateBtn.setEnabled(true);
        if (isState)
        	stateBtn.setSelection(true);
        stateBtn.setBounds(stationBtn.getBounds().x
                + stationBtn.getBounds().width + btnGapX,
                staStnGp.getBounds().y + labelGap, staBtnWidth, btnHeight);

        stateBtn.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {           
            	isState = true;  
            	handleStnMarkingRequestByBtn();
            }          		            	 	
        });
	}
    
    public void createTextArea(Composite parent) {

        // Text display area
        text = new Text(parent, SWT.V_SCROLL | SWT.H_SCROLL);
        
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        text.setLayoutData(data);
        Font font = text.getFont();
		FontData[] fontData = font.getFontData();
		for (int i = 0; i < fontData.length; i++) {
            // fontData[i].setHeight(12);
			fontData[i].setName("courier");
		}
		Font newFont = new Font(font.getDevice(), fontData);
		text.setFont(newFont);
	}
	
	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

    private String removeCR(String curStr) {
		int i = ASCII_CR_VAL;
        char asciiCr = (char) i;
		String newStr = curStr.replace(asciiCr, ' ');
		return newStr;
	}

    public void createTextModeGp(Composite parent) {
		printingHandle = HandlePrinting.getPrintHandle();
        Group textModeGp = new Group(parent, SWT.SHADOW_OUT);
		textModeGp.setText("Text Mode:");

		Button replaceBtn = new Button(textModeGp, SWT.RADIO);
		replaceBtn.setText("Replace");
        replaceBtn.setEnabled(true);
        replaceBtn.setBounds(textModeGp.getBounds().x + btnGapX,
                textModeGp.getBounds().y + labelGap, staBtnWidth, btnHeight);
		replaceBtn.setSelection(true);

        replaceBtn.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {           
				replaceText = true;    
				text.setText("");
				handleStnMarkingRequestByBtn();
			}          		            	 	
        });
		Button appendBtn = new Button(textModeGp, SWT.RADIO);
		appendBtn.setText("Append");
        appendBtn.setEnabled(true);
        appendBtn.setBounds(replaceBtn.getBounds().x
                + replaceBtn.getBounds().width + btnGapX,
                textModeGp.getBounds().y + labelGap, staBtnWidth, btnHeight);

        appendBtn.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {           
				replaceText = false;   
				nextBtn.setEnabled(false);
				prevBtn.setEnabled(false);
			}          		            	 	
        });

		// Push buttons for Previous text info
		prevBtn = new Button(textModeGp, SWT.PUSH);
		prevBtn.setText("Previous");
		prevBtn.setEnabled(false);
        prevBtn.setBounds(appendBtn.getBounds().x + appendBtn.getBounds().width
                + btnGapX, textModeGp.getBounds().y + labelGap, staBtnWidth,
                pushbtnHeight);

        prevBtn.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {           
				// Action to display previous text report  
                if ((currentTextReports != null)
                        && (currentTextReports.size() > currentTextIndex + 1)) {
                    String dispStr = removeCR((String) currentTextReports
                            .get(currentTextIndex + 1)[0]);
					String curText = text.getText();
					int endIndex = curText.indexOf("----");
                    if (endIndex != -1) {
                        curText = curText.substring(0, endIndex + 4);
                        text.setText(curText + "\n" + dispStr);
                    } else
						text.setText(dispStr);
					
					nextBtn.setEnabled(true);
					currentTextIndex++;
                    if (currentTextReports.size() <= currentTextIndex + 1) {
                        prevBtn.setEnabled(false);
					}
				}
			}          		            	 	
        });

		// Push buttons for Next text info
		nextBtn = new Button(textModeGp, SWT.PUSH);
		nextBtn.setText("Next");
		nextBtn.setEnabled(false);
        nextBtn.setBounds(prevBtn.getBounds().x + prevBtn.getBounds().width
                + btnGapX, textModeGp.getBounds().y + labelGap, staBtnWidth,
                pushbtnHeight);

        nextBtn.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {           
				// Action to display next text report  		
                if ((currentTextReports != null)
                        && (currentTextReports.size() > currentTextIndex)
                        && (currentTextIndex >= 1)) {
                    String dispStr = removeCR((String) currentTextReports
                            .get(currentTextIndex - 1)[0]);
					String curText = text.getText();
					int endIndex = curText.indexOf("----");
                    if (endIndex != -1) {
                        curText = curText.substring(0, endIndex + 4);
                        text.setText(curText + "\n" + dispStr);
                    } else
						text.setText(dispStr);
					prevBtn.setEnabled(true);
					currentTextIndex--;
                    if (currentTextIndex == 0) {
						nextBtn.setEnabled(false);
					}
				}
				
			}          		            	 	
        });

		// Push buttons for print text info
		Button printBtn = new Button(textModeGp, SWT.PUSH);
		printBtn.setText("Print");
        printBtn.setEnabled(true);
        printBtn.setBounds(nextBtn.getBounds().x + nextBtn.getBounds().width
                + btnGapX, textModeGp.getBounds().y + labelGap, staBtnWidth,
                pushbtnHeight);

        printBtn.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {           
				// Action to print text report  				
				printingHandle.handlePrint(text.getText());
			}          		            	 	
        });
		
	}
	
    public void widgetDefaultSelected(SelectionEvent se) {

	}

	/*
	 * invoked when widget is disposed
     * 
     * @see
     * org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt
     * .events.DisposeEvent)
	 */
    public void widgetDisposed(DisposeEvent event) {

	}

	@Override
	public void partActivated(IWorkbenchPart part) {
        if (part instanceof NcradaruiPaletteWindow) {
            NcradaruiResource rsc = NcradaruiResource.getNcradaruiResource();
            if (rsc != null)
                rsc.setEditable(true);
        }
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		partActivated(part);
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
        // System.out.println("NcradaruiPaletteWindow::Something closed: "+part.getClass().getCanonicalName()
        // );
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
        // System.out.println("Something Opened: "+part.getClass().getCanonicalName()
        // );
	}
	
	/**
	 * 
	 * @return the currently selected category on the palette
	 */
    public String getCurrentCategory() {
		return null;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void displayProduct(NcradarStationInfo StnPt) {
		NatlCntrsEditor mapEditor = NcradaruiResource.getMapEditor();
		
        if (StnPt != null && (mapEditor != null)) {
            // add RED "X" marker(s) on picked stn
            List<NcradarStationInfo> rtnStateStnLst = new ArrayList<NcradarStationInfo>();
            if (ncradaruiPaletteWindow.isState() == true) {

                List<NcradarStationInfo> stateStnLst = query
                        .getStateStationInfoList(ncradaruiPaletteWindow
                                .getCurrentProductName() + StnPt.getState());
                // need to filter out those stns does not have reports in DB
                // now, use points list for reference
                for (NcradarStationInfo stnInState : stateStnLst) {
                    for (NcradarStationInfo stnHasRpt : points) {
                        if (stnInState.getStnid().equals(stnHasRpt.getStnid()) == true) {
							rtnStateStnLst.add(stnInState);
							break;
						}
					}
				}
            } else {
				rtnStateStnLst.add(StnPt);
			}  
			Text text = ncradaruiPaletteWindow.getText();
            if (ncradaruiPaletteWindow.isReplaceText() == false) {
                // APPEND mode
                // List<NcradarStationInfo> prevPickedStnLst =
                // ncradaruiPaletteWindow.getncradaruiResource().getPickedStnPt();
                List<NcradarStationInfo> prevPickedStnLst = NcradaruiResource
                        .getNcradaruiResource().getPickedStnPt();
                if (prevPickedStnLst.size() > 0) {
                    if (rtnStateStnLst.addAll(prevPickedStnLst) == false) {
                        // System.out.println("handleMouseUp : add picked stn failed");
						return;
					} 
				}
            } else {
                // REPLACE mode
				text.setText("");
			}
            // ncradaruiPaletteWindow.getncradaruiResource().setPickedStnPt(rtnStateStnLst);
            NcradaruiResource.getNcradaruiResource().setPickedStnPt(
                    rtnStateStnLst);
			mapEditor.refresh();
            // ncradaruiModalTool.setModal();
            // QUERY DB now....Object[0] = Rawrecord text data, Object[1] =
            // issuesite
            List<List<Object[]>> rptLstList = query.getProductDataListList(
                    ncradaruiPaletteWindow.selectedGp,
                    ncradaruiPaletteWindow.getCurrentProductName(), StnPt,
                    ncradaruiPaletteWindow.getTimeCovered(),
                    ncradaruiPaletteWindow.isState(), null);
            if (rptLstList.isEmpty()) {
                if (ncradaruiPaletteWindow.isState())
                    text.append("--State " + StnPt.getState() + "--"
                            + ncradaruiPaletteWindow.getCurrentProductName()
                            + " Report (Station picked " + StnPt.getStnid()
                            + ")\n");
				else
                    text.append("--Text-- " + ": "
                            + ncradaruiPaletteWindow.getCurrentProductName()
                            + ": Reporting Station: (" + StnPt.getStnid()
                            + ") " + StnPt.getStnname() + "\n");
                if (ncradaruiPaletteWindow.getTimeCovered().getTimeRange() == 0)
					text.append("Report unavailable in database.\n");
				else			
                    text.append("Report unavailable within "
                            + ncradaruiPaletteWindow.getTimeCovered()
                                    .getTimeRange() + " hour(s) range.\n");

				ncradaruiPaletteWindow.enablePrevBtn(false);
            } else {
				String textToDisp;
				String textRawStr;
				StringBuilder textStr;
                if (ncradaruiPaletteWindow.isState()) {
                    // SelectBy State mode
                    textStr = new StringBuilder("--State " + StnPt.getState()
                            + "--"
                            + ncradaruiPaletteWindow.getCurrentProductName()
                            + " Report\n");

                    for (List<Object[]> lstObj : rptLstList) {
                        textStr.append("--Station "
                                + (String) (lstObj.get(0))[1] + "-- : "
                                + ncradaruiPaletteWindow.getCurrentProductName()
                                + "\n");
                        textRawStr = (String) (lstObj.get(0))[0];
						// remove CR before displaying
						textToDisp = removeCR(textRawStr);							
                        textStr.append(textToDisp + "\n");
					}

                    // When put text string to Text display, use "setText" but
                    // not "append" method, so, the text will show from top
                    if (ncradaruiPaletteWindow.isReplaceText() == false) {
						// get current text string from Text
                        StringBuilder textStr1 = new StringBuilder(
                                text.getText());
						textStr1.append(textStr.toString());
						text.setText(textStr1.toString());
                    } else
                        text.setText(textStr.toString());
                } else {
                    // SelectBy Station mode
					// "----" used as text header delimiter   
                    String textHeader = "--Text-- " + ": "
                            + ncradaruiPaletteWindow.getCurrentProductName()
                            + ": Reporting Station: (" + StnPt.getStnid()
                            + ") " + StnPt.getStnname() + "----" + "\n";
                    ncradaruiPaletteWindow.setCurrentTextReports(rptLstList
                            .get(0));

					int currentTextIndex = 0;
					ncradaruiPaletteWindow.setCurrentTextIndex(currentTextIndex);
                    textRawStr = (String) (rptLstList.get(0)
                            .get(currentTextIndex))[0];
					// remove CR before displaying
					textToDisp = removeCR(textRawStr);	
                    // When put text string to Text display, use "setText" but
                    // not "append" method, so, the text will show from top
                    if (ncradaruiPaletteWindow.isReplaceText() == false) {
                        // Append mode: get current text string from Text
                        StringBuilder textStr1 = new StringBuilder(
                                text.getText());
                        textStr1.append(textHeader + textToDisp);
						text.setText(textStr1.toString());
                    } else
                        // Replace mode
                        text.setText(textHeader + textToDisp);
                    if ((rptLstList.get(0).size() > 1)
                            && (ncradaruiPaletteWindow.isReplaceText() == true)) {
                        // System.out.println("debug 1 list size "+
                        // rptLstList.get(0).size());
						ncradaruiPaletteWindow.enablePrevBtn(true);
                    } else {
						ncradaruiPaletteWindow.enablePrevBtn(false);
					}
				}
			}
		}
	}
   
}
