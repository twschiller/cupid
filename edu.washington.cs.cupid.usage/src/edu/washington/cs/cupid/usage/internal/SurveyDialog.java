package edu.washington.cs.cupid.usage.internal;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class SurveyDialog extends TitleAreaDialog {

	private static final String SUBMISSION_MSG = "Your responses have been submitted.";

	public static final String DEV_SURVEY_URL = "https://catalyst.uw.edu/webq/survey/tws8/187176";
	
	private Button okButton = null;
	
	public SurveyDialog(Shell parentShell) {
		super(parentShell);
		setTitle("Cupid User Survey");
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		 GridData gridData = new GridData(SWT.CENTER, SWT.FILL, true, true);
		 parent.setLayoutData(gridData);
		 
		 okButton = createButton(parent, IDialogConstants.OK_ID, "OK", true);
		 okButton.setEnabled(false);
		 Button abort = createButton(parent, IDialogConstants.ABORT_ID, "I'm Not Interested", false);
		 Button ignore = createButton(parent, IDialogConstants.IGNORE_ID, "Remind Me in a Week", false);	
		 
		 abort.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				SurveyDialog.this.close();
				SurveyDialog.this.setReturnCode(IDialogConstants.ABORT_ID);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NOP
			}
		});
		 
		 ignore.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				SurveyDialog.this.close();
				SurveyDialog.this.setReturnCode(IDialogConstants.IGNORE_ID);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NOP	
			}
		 });
	}
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		setTitle("Cupid User Survey");
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);
		
		this.setMessage(
				"Please help us make Cupid better by completing a quick survey.",
				IMessageProvider.INFORMATION);
		
		Link surveyLink = new Link(parent, SWT.LEFT);
		surveyLink.setText("Survey will load below, or <a href=\"" + SurveyDialog.DEV_SURVEY_URL + "\">Click to Open in a Browser.</a>");
	
		GridData dSurvey = new GridData(SWT.FILL, SWT.NONE, true, false);
		surveyLink.setLayoutData(dSurvey);
		
		surveyLink.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					//  Open default external browser 
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(e.text));
				} catch (Exception ex) {
					Activator.getDefault().logError("Error loading Cupid survey in browser", ex);
				} 
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				//NOP
			}
		});
		
		try{
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			final Browser survey = new Browser(parent, SWT.BORDER);	
			survey.setText("<p>Loading Cupid user survey <a href=\"" + DEV_SURVEY_URL + "\">"+ DEV_SURVEY_URL + "</a>.</p>", true);
			
			if (!survey.setUrl(DEV_SURVEY_URL)){
				Activator.getDefault().logError("Error loading Cupid user survey", null);
				survey.setText("Error loading Cupid user survey");
			}
			
			data = new GridData(SWT.FILL, SWT.FILL, true, true);
			data.heightHint = 300;
			data.widthHint = 600;
			survey.setLayoutData(data);
			
			survey.addProgressListener(new ProgressListener() {
				@Override
				public void completed(ProgressEvent event) {
					String content = survey.getText();

					if (survey.getUrl().equals(DEV_SURVEY_URL) &&
							content.contains(SurveyDialog.SUBMISSION_MSG)){
						okButton.setEnabled(true);
					}
				}

				@Override
				public void changed(ProgressEvent event) {
					// NO OP
				}
			});
		} catch (SWTException ex){
			Activator.getDefault().logError("Error creating user survey browser", ex);
		}
		
		return parent;
	}
	
	public static Date addDaysToDate(final Date original, int numDays) {
		//http://stackoverflow.com/questions/3300328/add-1-week-to-a-date-which-way-is-preferred
		Date newDate = new Date(original.getTime());
	    GregorianCalendar calendar = new GregorianCalendar();
	    calendar.setTime(newDate);
	    calendar.add(Calendar.DATE, numDays);
	    newDate.setTime(calendar.getTime().getTime());
	    return newDate;
	}
}
