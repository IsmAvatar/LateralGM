LateralGM
=========
A cross-platform editor for Game Maker project files written in Java using Swing. You can find the latest build on the releases page. The source code is doxygen commented but [online documentation](http://enigma-dev.org/docs/Wiki/LateralGM) of the internals is available, including format specification details.

License
-------
This project is licensed under the GNU GPL v3 License. Please read the included LICENSE file or visit http://www.gnu.org/licenses for more information.

Building
--------

First, ensure you have initialized and updated the git submodules:

    git submodule update --init

This project can be built with Maven:

    mvn package

LateralGM can then be launched by running the created `.jar` file:

    java -jar target/LateralGM-1.8.10.jar

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
* Robert B. Colton <robertbcolton@hotmail.com> GMX format, Shaders, major bug fixes, and preferences panel.

Special Thanks
-------
* WittyCheeseBoy for extensive testing and finding lots of fun surprises.
* Josh@Dreamland <joshv@zoominternet.net> for his support and the Enigma Compiler.
* Everyone else from the ENIGMA forums, GMC forums, G-Creator forums, old LGM forums, etc.
 who helped out, gave tips, acknowledged LGM's presence, or otherwise aided in its creation,
 including but certainly not limited to (and pardon the many forgotten names):
* DeathFinderxx, Rusky, retep998, Porfirio, Polygone, pythonpoole, andrewmc, Yourself, Leif902,
GearGOD, roach, RhysAndrews, Bendodge, javaman1922, h0bbel, evilish, Natso, kkg
