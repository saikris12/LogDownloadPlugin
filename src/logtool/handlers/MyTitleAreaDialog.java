package logtool.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

import logtool.Activator;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MyTitleAreaDialog extends TitleAreaDialog {

	private Text txtUserName;
	private Text txtPassword;
	private Combo serverCombo;
	private Combo envCombo;
	private Combo clusterCombo;
	private Combo logCombo;
	private Combo serverTypesCombo;
	private Combo batchCompCombo;
	private Combo batchJobCombo;
	private Combo batchJobLogCombo;
	private Text logPath;

	private String userName;
	private String password;
	private String server;
	private String clusterName;
	private String logName;
	private String[] logList;
	private String serverType;
	private String batchComp;
	private String batchJob;
	private String batchJobLogName;
	private String[] batchLogList;
	private String logDownloadPath;

	// Minimum dialog width (in dialog units)
	private static final int MIN_DIALOG_WIDTH = 350;

	// Minimum dialog height (in dialog units)
	private static final int MIN_DIALOG_HEIGHT = 250;

	ILog logger = Activator.getDefault().getLog();

	public MyTitleAreaDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * The <code>TitleAreaDialog implementation of this
	 * <code>Window methods returns an initial size which is at least
	 * some reasonable minimum. Customizing the size of the Dialog
	 * 
	 * @return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		Point shellSize = super.getInitialSize();
		return new Point(Math.max(
				convertHorizontalDLUsToPixels(MIN_DIALOG_WIDTH), shellSize.x),
				Math.max(convertVerticalDLUsToPixels(MIN_DIALOG_HEIGHT),
						shellSize.y));
	}

	/*
	 * Customizing the button as per the requirement - All the button action is handled here in this method
	 * as I do not want to close the plugin window on clicking download.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);

		Button ok = getButton(IDialogConstants.OK_ID);
		ok.setText("Download");
		setButtonLayoutData(ok);
		ok.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				ProgressMonitorDialog pdialog = new ProgressMonitorDialog(getShell());
				try {
					pdialog.run(true, false, new IRunnableWithProgress(){
						public void run(IProgressMonitor monitor) {

							UnixHelper helper = new UnixHelper();
							try {
								if("appservers".equals(serverType)){
									if("All".equals(logName)){
										monitor.beginTask("Downloading from the server ...", logList.length-1);
										for(int i=1;i<logList.length;i++){
											helper.sftpLogFileToLocal(server, userName, password, clusterName, logList[i], logDownloadPath);
											monitor.worked(i);
										}
										logger.log(new Status(IStatus.OK, "Logs Downloaded : ", logName));
										MessageDialog.openInformation(getShell(), "Success", "Logs Downloaded");

									}
									else{
										monitor.beginTask("Downloading from the server ...", 1);
										helper.sftpLogFileToLocal(server, userName, password, clusterName, logName, logDownloadPath);
										monitor.worked(1);
										logger.log(new Status(IStatus.OK, "Log Downloaded : ", logName));
										MessageDialog.openInformation(getShell(), "Success", "Log Downloaded");
									}
								}
								else if("batchservers".equals(serverType)){
									if("All".equals(batchJobLogName)){
										monitor.beginTask("Downloading from the server ...", batchLogList.length-1);
										for(int i=1;i<batchLogList.length;i++){
											helper.sftpBatchLogFileToLocal(server, userName, password, batchComp, batchJob, batchLogList[i], logDownloadPath);
											monitor.worked(i);
										}
										logger.log(new Status(IStatus.OK, "Logs Downloaded : ", batchJobLogName));
										MessageDialog.openInformation(getShell(), "Success", "Logs Downloaded");

									}
									else{
										monitor.beginTask("Downloading from the server ...", 1);
										helper.sftpBatchLogFileToLocal(server, userName, password, batchComp, batchJob, batchJobLogName, logDownloadPath);
										monitor.worked(1);
										logger.log(new Status(IStatus.OK, "Log Downloaded : ", logName));
										MessageDialog.openInformation(getShell(), "Success", "Log Downloaded");
									}
								}
							} catch (Exception e) {
								logger.log(new Status(IStatus.ERROR, "Not able to connect to Server : ", server));
								logger.log(new Status(IStatus.ERROR, "Exception",e.getMessage()));
								MessageDialog.openInformation(getShell(), "Connection Failed", "Cannot connect to the Server due to incorrect credentials or Server may be down");
							}
							monitor.done();
						}
					});
				} catch (Exception e1) {
					MessageDialog.openInformation(getShell(), "Connection Failed", "Cannot connect to the Server due to incorrect credentials or Server may be down");
					e1.printStackTrace();
				}}
		});

		Button cancel = getButton(IDialogConstants.CANCEL_ID);
		cancel.setText("Cancel");
		setButtonLayoutData(cancel);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Log Download Plugin");
		setMessage("Select the server and Enter your credentials", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);
		createServerTypesCombo(container);
		createEnvCombo(container);
		createServerCombo(container);
		createUserName(container);
		createPassword(container);
		createConnectButton(container);
		createClusterDropdown(container);
		createLogDropdown(container);
		createBatchComponentsCombo(container);
		createBatchJobCombo(container);
		createBatchJobLogCombo(container);
		createBrowseOption(container);
		return area;
	}


	private void createServerTypesCombo(Composite container) {
		Label lbtEnvCombo = new Label(container, SWT.NONE);
		lbtEnvCombo.setText("Select Server Type");

		GridData dataLogCombo = new GridData(GridData.GRAB_HORIZONTAL);
		
		serverTypesCombo = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
		serverTypesCombo.setLayoutData(dataLogCombo);
		final Properties prop = new Properties();
		InputStream input = null;
		try {
			input = getClass().getResourceAsStream( "/servers.properties" );
			prop.load(input);
			Enumeration<Object> keys = prop.keys();
			ArrayList<String> serverTypeList = new ArrayList<String>();
			while(keys.hasMoreElements()){
				String serverType = keys.nextElement().toString();
				ILog logger = Activator.getDefault().getLog();
				logger.log(new Status(IStatus.OK, "ServerType : ", serverType));
				serverTypeList.add(serverType);
			}
			if(serverTypeList.size()>0){
				String[] serverTypesArray = new String[serverTypeList.size()];
				for(int j=0;j<serverTypeList.size();j++){
					serverTypesArray[j] = serverTypeList.get(j);
				}
				Arrays.sort(serverTypesArray);
				serverTypesCombo.setItems(serverTypesArray);
			}
			
			serverTypesCombo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					
					Properties envprop = new Properties();
					envCombo.setItems(new String[0]);
					String serverTypeSelected = serverTypesCombo.getText();
					InputStream inputStream = null;
					inputStream = getClass().getResourceAsStream( "/"+serverTypeSelected+".properties" );
					try {
						envprop.load(inputStream);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					Enumeration<Object> keys = envprop.keys();
					ArrayList<String> envList = new ArrayList<String>();
					while(keys.hasMoreElements()){
						String envType = keys.nextElement().toString();
						ILog logger = Activator.getDefault().getLog();
						logger.log(new Status(IStatus.OK, "ServerType : ", envType));
						envList.add(envType);
					}
					if(envList.size()>0){
						String[] envArray = new String[envList.size()];
						for(int j=0;j<envList.size();j++){
							envArray[j] = envList.get(j);
						}
						Arrays.sort(envArray);
						envCombo.setItems(envArray);
					}
					serverCombo.setItems(new String[0]);
					clusterCombo.setItems(new String[0]);
					logCombo.setItems(new String[0]);
					batchCompCombo.setItems(new String[0]);
					batchJobCombo.setItems(new String[0]);
					batchJobLogCombo.setItems(new String[0]);
				}
			});
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	private void createEnvCombo(Composite container) {
		Label lbtEnvCombo = new Label(container, SWT.NONE);
		lbtEnvCombo.setText("Select Environment");
		
		GridData dataEnv= new GridData();
		dataEnv.grabExcessHorizontalSpace = true;
		dataEnv.horizontalAlignment = GridData.FILL;
		
		envCombo = new Combo(container, SWT.READ_ONLY);
		envCombo.setLayoutData(dataEnv);
		
		final Properties envProp = new Properties();
		envCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				String serverType = serverTypesCombo.getText();
				String envSelected = envCombo.getText();
				InputStream inputStream = null;
				inputStream = getClass().getResourceAsStream( "/"+serverType+".properties" );
				try {
					envProp.load(inputStream);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String serverList = envProp.getProperty(envSelected);
				String[] serverArray = serverList.split(",");
				serverCombo.setItems(serverArray);
				clusterCombo.setItems(new String[0]);
				logCombo.setItems(new String[0]);
				batchCompCombo.setItems(new String[0]);
				batchJobCombo.setItems(new String[0]);
				batchJobLogCombo.setItems(new String[0]);
			}
		});
	}

	private void createServerCombo(Composite container) {
		Label lbtServerCombo = new Label(container, SWT.NONE);
		lbtServerCombo.setText("Select Server");

		GridData dataServer= new GridData();
		dataServer.grabExcessHorizontalSpace = true;
		dataServer.horizontalAlignment = GridData.FILL;

		serverCombo = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
		serverCombo.setLayoutData(dataServer);
		envCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				clusterCombo.setItems(new String[0]);
				logCombo.setItems(new String[0]);
				batchCompCombo.setItems(new String[0]);
				batchJobCombo.setItems(new String[0]);
				batchJobLogCombo.setItems(new String[0]);
			}
		});
	}


	private void createUserName(Composite container) {
		Label lbtFirstName = new Label(container, SWT.NONE);
		lbtFirstName.setText("User Name");

		GridData dataFirstName = new GridData();
		dataFirstName.grabExcessHorizontalSpace = true;
		dataFirstName.horizontalAlignment = GridData.FILL;

		txtUserName = new Text(container, SWT.BORDER);
		txtUserName.setLayoutData(dataFirstName);
	}

	private void createPassword(Composite container) {
		Label lbtLastName = new Label(container, SWT.NONE);
		lbtLastName.setText("Password");

		GridData dataPassword = new GridData();
		dataPassword.grabExcessHorizontalSpace = true;
		dataPassword.horizontalAlignment = GridData.FILL;

		txtPassword = new Text(container, SWT.BORDER | SWT.PASSWORD);
		txtPassword.setLayoutData(dataPassword);
	}


	private void createConnectButton(Composite container) {
		GridData dataConnect = new GridData();
		dataConnect.horizontalAlignment = GridData.FILL;
		Button connectButton = new Button(container, SWT.NONE);
		connectButton.setText("Connect To Server");
		connectButton.setLayoutData(dataConnect);
		connectButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Selection:
					UnixHelper helper = new UnixHelper();
					try {
						helper.connectToServer(serverCombo.getItem(serverCombo.getSelectionIndex()), txtUserName.getText(), txtPassword.getText());
						logger.log(new Status(IStatus.OK, "Connected to Server : ", serverCombo.getItem(serverCombo.getSelectionIndex())));
						if(serverTypesCombo.getText().equals("appservers")){
							batchCompCombo.setEnabled(false);
							batchJobCombo.setEnabled(false);
							batchJobLogCombo.setEnabled(false);
							clusterCombo.setEnabled(true);
							logCombo.setEnabled(true);
							String[] clusterList = helper.getLogClusterList(serverCombo.getItem(serverCombo.getSelectionIndex()), txtUserName.getText(), txtPassword.getText());
							Arrays.sort(clusterList);
							clusterCombo.setItems(clusterList);
							logCombo.setItems(new String[0]);
						}
						else if(serverTypesCombo.getText().equals("batchservers")){
							batchCompCombo.setEnabled(true);
							batchJobCombo.setEnabled(true);
							batchJobLogCombo.setEnabled(true);
							clusterCombo.setEnabled(false);
							logCombo.setEnabled(false);
							String[] batchComponentList = helper.getBatchComponentList(serverCombo.getItem(serverCombo.getSelectionIndex()), txtUserName.getText(), txtPassword.getText());
							batchCompCombo.setItems(batchComponentList);
							batchJobCombo.setItems(new String[0]);
							batchJobLogCombo.setItems(new String[0]);
						}
					} catch (Exception ex) {
						logger.log(new Status(IStatus.ERROR, "Not able to connect to Server : ", serverCombo.getItem(serverCombo.getSelectionIndex())));
						MessageDialog.openInformation(getShell(), "Connection Failed", "Cannot connect to the Server due to incorrect credentials or Server may be down");
					}
				}
				logPath.setText("C:\\users\\"+txtUserName.getText()+"\\logs");
			}
			
		});
		Label lbtDummy = new Label(container, SWT.NONE);
		lbtDummy.setText("");
	}

	private void createClusterDropdown(Composite container) {
		Label lbtClusterCombo = new Label(container, SWT.NONE);
		lbtClusterCombo.setText("Select Cluster");

		clusterCombo = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
		clusterCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String clusterSelected  = clusterCombo.getText();
				UnixHelper helper = new UnixHelper();
				try {
					String[] logFileList = helper.getLogFilesList(serverCombo.getItem(serverCombo.getSelectionIndex()), txtUserName.getText(), txtPassword.getText(), clusterSelected);
					Arrays.sort(logFileList);
					logCombo.setItems(logFileList);

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	}

	private void createLogDropdown(Composite container) {
		Label lbtLogCombo = new Label(container, SWT.NONE);
		lbtLogCombo.setText("Select Log file to Download");

		GridData dataLogCombo = new GridData();
		dataLogCombo.grabExcessHorizontalSpace = true;
		dataLogCombo.horizontalAlignment = GridData.FILL;

		logCombo = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
		logCombo.setLayoutData(dataLogCombo);
	}


	private void createBatchComponentsCombo(Composite container) {
		Label lbtBatchCompCombo = new Label(container, SWT.NONE);
		lbtBatchCompCombo.setText("Select Batch Component");

		GridData dataBatchCombo = new GridData();
		dataBatchCombo.grabExcessHorizontalSpace = true;
		dataBatchCombo.horizontalAlignment = GridData.FILL;
		batchCompCombo = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
		batchCompCombo.setLayoutData(dataBatchCombo);

		batchCompCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String batchCompSelected  = batchCompCombo.getText();
				UnixHelper helper = new UnixHelper();
				try {
					String[] batchJobList = helper.getBatchJobList(serverCombo.getItem(serverCombo.getSelectionIndex()), txtUserName.getText(), txtPassword.getText(), batchCompSelected);
					//Arrays.sort(batchJobList);
					batchJobCombo.setItems(batchJobList);

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	}


	private void createBatchJobCombo(Composite container) {
		Label lbtBatchJobCombo = new Label(container, SWT.NONE);
		lbtBatchJobCombo.setText("Select Batch Job");

		GridData dataBatchJobCombo = new GridData();
		dataBatchJobCombo.grabExcessHorizontalSpace = true;
		dataBatchJobCombo.horizontalAlignment = GridData.FILL;
		batchJobCombo = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
		batchJobCombo.setLayoutData(dataBatchJobCombo);

		batchJobCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String batchCompSelected  = batchCompCombo.getText();
				String batchJobSelected  = batchJobCombo.getText();
				UnixHelper helper = new UnixHelper();
				try {
					String[] batchJobLogList = helper.getBatchJobLogList(serverCombo.getItem(serverCombo.getSelectionIndex()), txtUserName.getText(), txtPassword.getText(), batchCompSelected, batchJobSelected);
					//Arrays.sort(batchJobLogList);
					batchJobLogCombo.setItems(batchJobLogList);

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	}

	private void createBatchJobLogCombo(Composite container) {
		Label lbtBatchLogCombo = new Label(container, SWT.NONE);
		lbtBatchLogCombo.setText("Select Log file to Download");

		GridData dataBatchLogCombo = new GridData();
		dataBatchLogCombo.grabExcessHorizontalSpace = true;
		dataBatchLogCombo.horizontalAlignment = GridData.FILL;

		batchJobLogCombo = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
		batchJobLogCombo.setLayoutData(dataBatchLogCombo);
	}

	private void createBrowseOption(Composite container){
		Button browse = new Button(container, SWT.PUSH);
        browse.setText("Download To ...");
        browse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false,1,0));
        browse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.NULL);
                String path = dialog.open();
                if (path != null) {
                	logPath.setText(path);
                }
            }

        });
        
        GridData dataLogDownloadPath = new GridData();
		dataLogDownloadPath.grabExcessHorizontalSpace = true;
		dataLogDownloadPath.horizontalAlignment = GridData.FILL;

		logPath = new Text(container, SWT.BORDER);
		logPath.setLayoutData(dataLogDownloadPath);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	/*
	 * This method saves all the form data into string objects so that
	 * I can access these values once the Download button is clicked. 
	 */
	private void saveInput() {
		userName = txtUserName.getText();
		password = txtPassword.getText();
		server = serverCombo.getItem(serverCombo.getSelectionIndex());
		serverType = serverTypesCombo.getText();
		if("appservers".equals(serverType)){
			clusterName = clusterCombo.getItem(clusterCombo.getSelectionIndex());
			logName = logCombo.getItem(logCombo.getSelectionIndex());
			if("All".equals(logName)){
				logList=logCombo.getItems();
			}
		}
		else if("batchservers".equals(serverType)){
			batchComp = batchCompCombo.getText();
			batchJob = batchJobCombo.getText();
			batchJobLogName = batchJobLogCombo.getText();
			if("All".equals(batchJobLogName)){
				batchLogList=batchJobLogCombo.getItems();
			}
		}
		logDownloadPath = logPath.getText();
	}

	@Override
	protected void okPressed() {
		saveInput();
		setReturnCode(OK);
		//super.okPressed();
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getServer() {
		return server;
	}

	public String getClusterName() {
		return clusterName;
	}

	public String getLogName() {
		return logName;
	}

	public String[] getLogList() {
		return logList;
	}
	
	public String getServerType(){
		return serverType;
	}

	public String getBatchComp() {
		return batchComp;
	}

	public String getBatchJob() {
		return batchJob;
	}
	
	public String getBatchJobLogName() {
		return batchJobLogName;
	}

	public String[] getBatchLogList() {
		return batchLogList;
	}
	
	public String getLogDownloadPath() {
		return logDownloadPath;
	}
}
