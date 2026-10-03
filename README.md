# LockIn---CSS123P

SHARED METHODS (the only things other files should call)

ramiraTrackerPanel
  boolean isRestricted(String site)
  void logTrackedSite(String site)

lauriceTimerPanel
  boolean isWorkSession()

lauriceSettingsPanel
  getters for the timer sound and the warning message version
  (the slides show Sound 1/2 and Version 1/2/3, so the old
  isSoundOn/isWarningOn names don't fit anymore)

MainFrame
  void setThemeColors(Color top, Color bottom)

jhenicaApiServer
  start(), plus a way to hand it the tracker and timer to ask