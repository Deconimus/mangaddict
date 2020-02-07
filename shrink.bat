@echo off
call jarShrink "C:\Program Files\MangAddict\MangAddict.jar" -o "C:\Program Files\MangAddict\MangAddict.jar" -k net.java.games.input -k mangaLib -k org.newdawn.slick.opengl
call jarShrink "C:\Program Files\MangAddict\res\bin\mangadl\mangadl.jar" -o "C:\Program Files\MangAddict\res\bin\mangadl\mangadl.jar" -k mangaLib -k org.newdawn.slick.opengl