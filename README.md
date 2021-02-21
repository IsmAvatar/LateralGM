LateralGM [![Travis CI Build Status](https://travis-ci.org/IsmAvatar/lateralgm.svg?branch=master)](https://travis-ci.org/github/IsmAvatar/LateralGM)
=========
A cross-platform editor for Game Maker project files written in Java using Swing. The ![releases page](https://github.com/IsmAvatar/LateralGM/releases) has a jar for the latest build. The source code is doxygen commented but [online documentation](http://enigma-dev.org/docs/Wiki/LateralGM) of the internals is available, including format specification details.

LateralGM is also maintained by the ![ENIGMA](https://github.com/enigma-dev/enigma-dev) developers, so please consider donating to them on Patreon!

[![Patreon](https://enigma-dev.org/site/images/v4/patreon.png)](https://www.patreon.com/m/enigma_dev) 

Building From Source
=======
You don't actually have to build the project yourself. There are already jars available on the ![releases page](https://github.com/IsmAvatar/LateralGM/releases) which you can run in Java 1.7 or higher. If you are looking to develop the source, then you will need to make sure you recursively clone to properly initialize all submodules (e.g, JoshEdit).

```git clone --recursive https://github.com/IsmAvatar/LateralGM.git```

You can then run GNU Make to build a jar using the `javac` compiler. Alternatively, you can import the project into an Eclipse Workspace to build with the `ecj` compiler.

License
-------
This project is licensed under the GNU GPL v3 License. Please read the included LICENSE file or visit http://www.gnu.org/licenses for more information.

Contributors
-------
* IsmAvatar <IsmAvatar@gmail.com> as project leader, lead programmer, file format expert,
 philosopher, project politics, and most publicity (both research and release).
* Clam <clamisgood@gmail.com> for most of the Save and Load code
 which I then proceeded to break and refix, and most of the MDI desktop,
 and the ResourceFrame and generics, and most of our visual components.
* Quadduc <quadduc@gmail.com> for technical things
 like format fix, license, legality, bug fixes, and the great GM7 breakthrough;
 almost all of our lib icons, much work on the Frames, and string externalization.
* TGMG <thegamemakerguru@gmail.com> for various programming,
 and finding ways to break things that I thought couldn't be broken
 and finding SVN, which helped teamwork and version control.
* Josh Ventura <JoshV10@gmail.com> provided some icons and algorithm assistance.
* Robert B. Colton added GMX format, Shaders, major bug fixes, and preferences panel.

Special Thanks
-------
* WittyCheeseBoy for extensive testing and finding lots of fun surprises.
* Josh@Dreamland <joshv@zoominternet.net> for his support and the Enigma Compiler.
* Everyone else from the ENIGMA forums, GMC forums, G-Creator forums, old LGM forums, etc.
 who helped out, gave tips, acknowledged LGM's presence, or otherwise aided in its creation,
 including but certainly not limited to (and pardon the many forgotten names):
* DeathFinderxx, Rusky, retep998, Porfirio, Polygone, pythonpoole, andrewmc, Yourself, Leif902,
GearGOD, roach, RhysAndrews, Bendodge, javaman1922, h0bbel, evilish, Natso, kkg
