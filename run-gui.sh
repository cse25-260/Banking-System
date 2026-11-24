#!/usr/bin/env bash
# Starts the virtual desktop (only if it isn't running) and then the Banking System app.
export DISPLAY=:1

# Install the tools once (skipped if already installed)
if ! command -v Xvfb >/dev/null 2>&1 || ! command -v websockify >/dev/null 2>&1; then
  sudo apt-get update && sudo apt-get install -y xvfb x11vnc novnc websockify fluxbox \
    libgtk-3-0 libxtst6 libxxf86vm1 libgl1 maven
fi

# Make the plain port-6080 address open the desktop directly
echo '<meta http-equiv="refresh" content="0;url=vnc.html?autoconnect=true&resize=scale">' \
  | sudo tee /usr/share/novnc/index.html >/dev/null

# Start each part only if it is not already running
if ! pgrep -x Xvfb >/dev/null; then
  rm -f /tmp/.X1-lock /tmp/.X11-unix/X1
  nohup Xvfb :1 -screen 0 1280x800x24 >/tmp/xvfb.log 2>&1 &
  sleep 2
fi
if ! pgrep -x fluxbox >/dev/null; then
  nohup fluxbox >/tmp/fluxbox.log 2>&1 &
fi
if ! pgrep -x x11vnc >/dev/null; then
  nohup x11vnc -display :1 -nopw -forever -shared -rfbport 5900 >/tmp/x11vnc.log 2>&1 &
fi
if ! pgrep -f "websockify.*6080" >/dev/null; then
  nohup websockify --web /usr/share/novnc 6080 localhost:5900 >/tmp/websockify.log 2>&1 &
fi
sleep 2

echo "Desktop is ready: open port 6080 in the Ports tab."
mvn -q javafx:run
