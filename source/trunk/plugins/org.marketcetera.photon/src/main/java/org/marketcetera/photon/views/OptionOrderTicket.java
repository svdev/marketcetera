package org.marketcetera.photon.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Section;
import org.marketcetera.photon.EclipseUtils;
import org.marketcetera.photon.PhotonPlugin;
import org.marketcetera.photon.parser.ILexerFIXImage;
import org.marketcetera.photon.parser.OpenCloseImage;
import org.marketcetera.photon.parser.OrderCapacityImage;
import org.marketcetera.photon.parser.PutOrCallImage;
import org.marketcetera.photon.preferences.CustomOrderFieldPage;
import org.marketcetera.quickfix.FIXMessageUtil;

import quickfix.Message;
import quickfix.field.Account;
import quickfix.field.OpenClose;
import quickfix.field.OrderCapacity;

/**
 * Option order ticket view.
 * 
 * @author andrei.lissovski@softwaregoodness.com
 */
public class OptionOrderTicket extends AbstractOrderTicket implements
		IOptionOrderTicket {

	public static String ID = "org.marketcetera.photon.views.OptionOrderTicket"; //$NON-NLS-1$

	private static final String NEW_OPTION_ORDER = "New Option Order";

	private static final String REPLACE_OPTION_ORDER = "Replace Option Order";

	private Combo expireMonthCombo = null;

	private Text strikeText = null;

	private Combo putOrCallCombo = null;

	private Combo expireYearCombo = null;

	private Section otherExpandableComposite;

	private Text accountText;

	private Combo orderCapacityCombo;

	private Combo openCloseCombo;

	private OptionOrderTicketController optionOrderTicketController;

	public OptionOrderTicket() {
	}

	public OptionOrderTicketController getOptionOrderTicketController() {
		return optionOrderTicketController;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		optionOrderTicketController = new OptionOrderTicketController();
		optionOrderTicketController.bind(this);
	}

	@Override
	public void dispose() {
		safelyDispose(optionOrderTicketController);

		super.dispose();
	}

	@Override
	protected void updateOutermostFormTitle(Message targetOrder) {
		checkOutermostFormInitialized();
		if (targetOrder == null
				|| !FIXMessageUtil.isCancelReplaceRequest(targetOrder)) {
			outermostForm.setText(NEW_OPTION_ORDER);
		} else {
			outermostForm.setText(REPLACE_OPTION_ORDER);
		}
	}

	@Override
	protected int getNumColumnsInForm() {
		return 9;
	}

	@Override
	protected void createFormContents() {
		Composite formBody = outermostForm.getBody();
		getFormToolkit().createLabel(formBody, "Side");
		getFormToolkit().createLabel(formBody, "Quantity");
		getFormToolkit().createLabel(formBody, "Symbol");
		getFormToolkit().createLabel(formBody, "Expiration");
		getFormToolkit().createLabel(formBody, "Strike");
		getFormToolkit().createLabel(formBody, "Year");
		getFormToolkit().createLabel(formBody, "C/P");
		getFormToolkit().createLabel(formBody, "Price");
		getFormToolkit().createLabel(formBody, "TIF");

		orderTicketViewPieces.createSideInput();
		orderTicketViewPieces.createQuantityInput();
		orderTicketViewPieces.createSymbolInput();
		createExpireMonthBorderComposite();
		createStrikeBorderComposite();
		createExpireYearBorderComposite();
		createPutOrCallBorderComposite();
		orderTicketViewPieces.createPriceInput();
		orderTicketViewPieces.createTifInput();

		createSendAndCancelButtons();

		createOtherExpandableComposite();
		customFieldsViewPieces.createCustomFieldsExpandableComposite(6);
		createBookSection();

		customFieldsViewPieces.updateCustomFields(PhotonPlugin.getDefault()
				.getPreferenceStore().getString(
						CustomOrderFieldPage.CUSTOM_FIELDS_PREFERENCE));

		outermostForm.pack(true);

		adjustTabOrder();
	}

	private void adjustTabOrder() {
		{
			ArrayList<Control> tabOrder = new ArrayList<Control>();
			tabOrder.add(orderTicketViewPieces.getSideCombo());
			tabOrder.add(orderTicketViewPieces.getQuantityText());
			tabOrder.add(orderTicketViewPieces.getSymbolText());
			tabOrder.add(expireMonthCombo);
			tabOrder.add(strikeText);
			tabOrder.add(expireYearCombo);
			tabOrder.add(putOrCallCombo);
			tabOrder.add(orderTicketViewPieces.getPriceText());
			tabOrder.add(orderTicketViewPieces.getTifCombo());

			tabOrder.add(otherExpandableComposite);
			tabOrder.add(sendButton.getParent());

			Composite parent = orderTicketViewPieces.getSideCombo().getParent();
			parent.setTabList(tabOrder.toArray(new Control[0]));
		}

		{
			ArrayList<Control> tabOrder = new ArrayList<Control>();
			tabOrder.add(openCloseCombo);
			tabOrder.add(orderCapacityCombo);
			Composite parent = openCloseCombo.getParent();
			parent.setTabList(tabOrder.toArray(new Control[0]));
		}

		{
			ArrayList<Control> tabOrder = new ArrayList<Control>();
			tabOrder.add(sendButton);
			tabOrder.add(cancelButton);
			Composite parent = sendButton.getParent();
			parent.setTabList(tabOrder.toArray(new Control[0]));
		}
	}

	private void createExpireMonthBorderComposite() {
		checkOutermostFormInitialized();
		expireMonthCombo = new Combo(outermostForm.getBody(), SWT.BORDER);
		// todo: Dynamically populate expiration choices from market data

		SimpleDateFormat formatter = new SimpleDateFormat("MMM");
		GregorianCalendar calendar = new GregorianCalendar();
		final int minMonth = calendar.getMinimum(Calendar.MONTH);
		final int maxMonth = calendar.getMaximum(Calendar.MONTH);
		for (int month = minMonth; month <= maxMonth; ++month) {
			calendar.set(Calendar.MONTH, month);
			java.util.Date monthTime = calendar.getTime();
			String monthStr = formatter.format(monthTime);
			monthStr = monthStr.toUpperCase();
			expireMonthCombo.add(monthStr);
		}

		orderTicketViewPieces.addInputControlErrorDecoration(expireMonthCombo);
	}

	private void addSelectAllFocusListener(Control control) {
		control.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				((Text) e.widget).selectAll();
			}
		});
	}

	private void createStrikeBorderComposite() {
		strikeText = getFormToolkit().createText(outermostForm.getBody(), null,
				SWT.SINGLE | SWT.BORDER);
		addSelectAllFocusListener(strikeText);

		GridData textGridData = new GridData();
		Point sizeHint = EclipseUtils
				.getTextAreaSize(strikeText, null, 10, 1.0);
		// textGridData.heightHint = sizeHint.y;
		textGridData.widthHint = sizeHint.x;
		strikeText.setLayoutData(textGridData);

		orderTicketViewPieces.addInputControlErrorDecoration(strikeText);
	}

	private void createExpireYearBorderComposite() {
		expireYearCombo = new Combo(outermostForm.getBody(), SWT.BORDER);
		// todo: Dynamically populate year choices from market data.
		final int maxYear = 12;
		for (int currentYear = 7; currentYear <= maxYear; ++currentYear) {
			StringBuilder year = new StringBuilder();
			if (currentYear < 10) {
				year.append("0");
			}
			year.append(currentYear);
			expireYearCombo.add(year.toString());
		}

		orderTicketViewPieces.addInputControlErrorDecoration(expireYearCombo);
	}

	private void createPutOrCallBorderComposite() {
		putOrCallCombo = new Combo(outermostForm.getBody(), SWT.BORDER);
		putOrCallCombo.add(PutOrCallImage.PUT.getImage());
		putOrCallCombo.add(PutOrCallImage.CALL.getImage());

		orderTicketViewPieces.addInputControlErrorDecoration(putOrCallCombo);
	}

	private GridLayout createStandardBorderGridLayout() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 2;
		gridLayout.verticalSpacing = 2;
		gridLayout.horizontalSpacing = 2;
		gridLayout.marginHeight = 2;
		gridLayout.numColumns = 1;
		return gridLayout;
	}

	// todo: Duplicated code from StockOrderTicket, substantially modified
	/**
	 * This method initializes customFieldsExpandableComposite
	 * 
	 */
	private void createOtherExpandableComposite() {
		GridData gridData3 = new GridData();
		gridData3.horizontalSpan = 2;
		gridData3.verticalAlignment = GridData.BEGINNING;
		// gridData3.grabExcessHorizontalSpace = true;
		gridData3.horizontalAlignment = GridData.FILL;
		otherExpandableComposite = getFormToolkit().createSection(
				outermostForm.getBody(), Section.TITLE_BAR | Section.TWISTIE);
		otherExpandableComposite.setText("Other");
		otherExpandableComposite.setExpanded(true);
		otherExpandableComposite.setLayoutData(gridData3);

		Composite otherComposite = getFormToolkit().createComposite(
				otherExpandableComposite);
		GridLayout otherGridLayout = createStandardBorderGridLayout();
		otherGridLayout.numColumns = 2;
		otherComposite.setLayout(otherGridLayout);
		otherExpandableComposite.setClient(otherComposite);

		Label accountLabel = getFormToolkit().createLabel(otherComposite,
				"Account:");
		accountLabel.setLayoutData(createStandardSingleColumnGridData());
		accountText = getFormToolkit().createText(otherComposite, "");
		addFixFieldEntry(otherComposite, accountText, Account.FIELD);

		Label openCloseLabel = getFormToolkit().createLabel(otherComposite,
				"Open/Close");
		openCloseLabel.setLayoutData(createStandardSingleColumnGridData());
		openCloseCombo = createFixFieldImageComboEntry(otherComposite,
				"OpenClose", OpenClose.FIELD, OpenCloseImage.values());
		orderTicketViewPieces.addInputControlErrorDecoration(openCloseCombo);

		Label capacityLabel = getFormToolkit().createLabel(otherComposite,
				"Capacity");
		capacityLabel.setLayoutData(createStandardSingleColumnGridData());
		orderCapacityCombo = createFixFieldImageComboEntry(otherComposite,
				"Capacity", OrderCapacity.FIELD, OrderCapacityImage.values());
		orderTicketViewPieces
				.addInputControlErrorDecoration(orderCapacityCombo);
	}

	private void addComboChoicesFromLexerEnum(Combo combo,
			ILexerFIXImage[] choices) {
		for (ILexerFIXImage choice : choices) {
			combo.add(choice.getImage());
		}
	}

	private Combo createFixFieldImageComboEntry(Composite parent,
			String fieldNameForValidator, int fixFieldNumber,
			ILexerFIXImage[] choices) {

		Combo combo = new Combo(parent, SWT.BORDER);
		combo.setLayoutData(createStandardSingleColumnGridData());
		addComboChoicesFromLexerEnum(combo, choices);

		return combo;
	}

	private GridData createStandardSingleColumnGridData() {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.LEFT;
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalSpan = 1;
		gridData.verticalSpan = 1;
		return gridData;
	}

	private void addFixFieldEntry(Composite targetComposite, Text textControl,
			int fixFieldNumber) {

		Point sizeHint = EclipseUtils.getTextAreaSize(targetComposite, null,
				10, 1.0);

		GridData textGridData = createStandardSingleColumnGridData();
		textGridData.widthHint = sizeHint.x;
		textGridData.heightHint = sizeHint.y;
		textControl.setLayoutData(textGridData);

		getFormToolkit().paintBordersFor(targetComposite);
	}

	public static OptionOrderTicket getDefault() {
		OptionOrderTicket orderTicket = (OptionOrderTicket) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView(OptionOrderTicket.ID);

		return orderTicket;
	}

	public Text getAccountText() {
		return accountText;
	}

	public Combo getExpireMonthCombo() {
		return expireMonthCombo;
	}

	public Combo getExpireYearCombo() {
		return expireYearCombo;
	}

	public Combo getOpenCloseCombo() {
		return openCloseCombo;
	}

	public Combo getOrderCapacityCombo() {
		return orderCapacityCombo;
	}

	public Combo getPutOrCallCombo() {
		return putOrCallCombo;
	}

	public Text getStrikeText() {
		return strikeText;
	}

}