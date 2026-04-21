[Setup]
AppName=Voucher Management System
AppVersion=1.0
AppPublisher=VMS
DefaultDirName={autopf}\VMS
DefaultGroupName=VMS
OutputDir=installer
OutputBaseFilename=VMS-Setup
SetupIconFile=vms-logo.ico
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
ArchitecturesInstallIn64BitMode=x64
PrivilegesRequired=admin

[Files]
Source: "dist\VMS\*"; DestDir: "{app}"; Flags: recursesubdirs createallsubdirs

[Icons]
Name: "{group}\VMS"; Filename: "{app}\VMS.exe"; IconFilename: "{app}\VMS.exe"
Name: "{group}\Désinstaller VMS"; Filename: "{uninstallexe}"
Name: "{autodesktop}\VMS"; Filename: "{app}\VMS.exe"; IconFilename: "{app}\VMS.exe"

[Run]
Filename: "{app}\VMS.exe"; Description: "Lancer VMS"; Flags: nowait postinstall skipifsilent