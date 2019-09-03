# aps-cmd-tool

A tool to interact with APS Bus of Odin Automation

## Usage

First, you must setup an environment variable to define a host with Odin Automation

```
$ export OA='10.254.252.42'
```

Then you can request APS Bus with commands like

```
$ aps get resources
```

```
$ aps get resources/2f9fd3ee-6047-442d-be72-737151967c08/livenessProbe
```

## License

Copyright © 2019 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
