; ================================================================
; FastPass Web Application - Inno Setup Script
; SMARTCORE Inc.
; ================================================================

#define MyAppName "FastPass Web"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "SMARTCORE Inc."
#define MyAppURL "http://localhost:50000"
#define MyServiceName "FastPassWeb"

[Setup]
AppId={{A1B2C3D4-E5F6-7890-ABCD-EF1234567890}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL=https://www.smartcoreinc.com
DefaultDirName={autopf}\SMARTCORE\FastPass Web
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
OutputDir=output
OutputBaseFilename=FastPassSetup-{#MyAppVersion}
Compression=lzma2/ultra64
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=admin
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
MinVersion=10.0
SetupLogging=yes
UninstallDisplayIcon={app}\fastpass-service.exe
DisableProgramGroupPage=yes

; License file (optional - uncomment if you have one)
; LicenseFile=license.txt

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "korean"; MessagesFile: "compiler:Languages\Korean.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; Spring Boot JAR
Source: "staging\fphps_web_example.jar"; DestDir: "{app}"; Flags: ignoreversion

; Application properties
Source: "staging\application.properties"; DestDir: "{app}"; Flags: ignoreversion onlyifdoesntexist

; Bundled JRE (jlink)
Source: "staging\jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs

; WinSW service wrapper
Source: "staging\fastpass-service.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "staging\fastpass-service.xml"; DestDir: "{app}"; Flags: ignoreversion

; Service management scripts
Source: "scripts\install-service.bat"; DestDir: "{app}\scripts"; Flags: ignoreversion
Source: "scripts\uninstall-service.bat"; DestDir: "{app}\scripts"; Flags: ignoreversion

[Dirs]
Name: "{app}\data"; Permissions: users-modify
Name: "{app}\log"; Permissions: users-modify

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\open-browser.bat"; IconFilename: "{app}\fastpass-service.exe"; Comment: "Open FastPass Web Application"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\open-browser.bat"; IconFilename: "{app}\fastpass-service.exe"; Tasks: desktopicon; Comment: "Open FastPass Web Application"

[Run]
; Install and start the service after installation
Filename: "{app}\scripts\install-service.bat"; StatusMsg: "Installing Windows service..."; Flags: runhidden waituntilterminated
; Open browser after installation (optional)
Filename: "{app}\open-browser.bat"; Description: "Open FastPass Web in browser"; Flags: nowait postinstall skipifsilent shellexec

[UninstallRun]
; Stop and remove the service before uninstallation
Filename: "{app}\scripts\uninstall-service.bat"; RunOnceId: "UninstallService"; Flags: runhidden waituntilterminated

[Code]
const
  FIREWALL_RULE_NAME = 'FastPass Web Application';
  FIREWALL_PORT = '50000';

// Add firewall rule during installation
procedure AddFirewallRule();
var
  ResultCode: Integer;
begin
  Exec('netsh', 'advfirewall firewall add rule name="' + FIREWALL_RULE_NAME + '" dir=in action=allow protocol=tcp localport=' + FIREWALL_PORT, '', SW_HIDE, ewWaitUntilTerminated, ResultCode);
  if ResultCode = 0 then
    Log('Firewall rule added successfully')
  else
    Log('Failed to add firewall rule (code: ' + IntToStr(ResultCode) + ')');
end;

// Remove firewall rule during uninstallation
procedure RemoveFirewallRule();
var
  ResultCode: Integer;
begin
  Exec('netsh', 'advfirewall firewall delete rule name="' + FIREWALL_RULE_NAME + '"', '', SW_HIDE, ewWaitUntilTerminated, ResultCode);
  if ResultCode = 0 then
    Log('Firewall rule removed successfully')
  else
    Log('Failed to remove firewall rule (code: ' + IntToStr(ResultCode) + ')');
end;

// Create browser launcher script
procedure CreateBrowserLauncher();
var
  FileName: String;
begin
  FileName := ExpandConstant('{app}\open-browser.bat');
  SaveStringToFile(FileName, '@echo off' + #13#10 + 'start http://localhost:50000' + #13#10, False);
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then
  begin
    AddFirewallRule();
    CreateBrowserLauncher();
  end;
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
begin
  if CurUninstallStep = usPostUninstall then
  begin
    RemoveFirewallRule();
  end;
end;

// Check if the service is running before uninstall
function InitializeUninstall(): Boolean;
begin
  Result := True;
end;
