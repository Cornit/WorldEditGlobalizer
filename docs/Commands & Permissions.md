Commands & Permissions
======================
Table of Contents
-----------------

- [Commands](#commands)
- [Permissions](#permissions)

Commands
--------

| Command                                                                          | Description                                          | Permissions                                      |
| -------------------------------------------------------------------------------- | ---------------------------------------------------- | ------------------------------------------------ |
| [`/weg clipboard`](#weg-clipboard)                                               | commands regarding clipboards                        |                                                  |
| [`/weg clipboard clear [player name]`](#weg-clipboard-clear-player-name)         | clears your/someone else's clipboard                 | `worldeditglobalizer.command.clipboard.clear`    |
| [`/weg clipboard download`](#weg-clipboard-download)                             | download your clipboard                              | `worldeditglobalizer.command.clipboard.download` |
| [`/weg clipboard info [player name]`](#weg-clipboard-info-player-name)           | show information about your/someone else's clipboard | `worldeditglobalizer.command.clipboard.info`     |
| [`/weg clipboard upload`](#weg-clipboard-upload)                                 | upload your clipboard                                | `worldeditglobalizer.command.clipboard.upload`   |
| [`/weg reload`](#weg-reload)                                                     | reloads all configs                                  | `worldeditglobalizer.command.reload`             |
| [`/weg schematic`](#weg-schematic)                                               | commands regarding schematics                        |                                                  |
| [`/weg schematic delete <schematic name>`](#weg-schematic-delete-schematic-name) | deletes a schematic                                  | `worldeditglobalizer.command.schematic.delete`   |
| [`/weg schematic list [-p: page]`](#weg-schematic-list--p-page)                  | a list of saved schematics                           | `worldeditglobalizer.command.schematic.list`     |
| [`/weg schematic load <schematic name>`](#weg-schematic-load-schematic-name)     | load a schematic in yout clipboard                   | `worldeditglobalizer.command.schematic.load`     |
| [`/weg schematic save <schematic name>`](#weg-schematic-save-schematic-name)     | save your clipboard to a schematic file              | `worldeditglobalizer.command.schematic.save`     |

---

### /weg clipboard

**Command:** `/weg clipboard`
<br/>
**Description:** `commands regarding clipboards`
<br/>
**Permissions:** none
<br/>

**Arguments:** none

---

### /weg clipboard clear [player name]

**Command:** `/weg clipboard clear [player name]`
<br/>
**Description:** `clears your/someone else's clipboard`
<br/>
**Permissions:**

- `worldeditglobalizer.command.clipboard.clear`
  <br/>

**Arguments:**

| Position |  Argument   | Description | Optional? | Boolean Flag? | Value Flag? | Default value |                     Permissions                     |
|:--------:|:-----------:|:-----------:|:---------:|:-------------:|:-----------:|:-------------:|:---------------------------------------------------:|
|    1     | player name |      -      |    yes    |      no       |     no      |               | `worldeditglobalizer.command.clipboard.clear.other` |

---

### /weg clipboard download

**Command:** `/weg clipboard download`
<br/>
**Description:** `download your clipboard`
<br/>
**Permissions:**

- `worldeditglobalizer.command.clipboard.download`
  <br/>

**Arguments:** none

---

### /weg clipboard info [player name]

**Command:** `/weg clipboard info [player name]`
<br/>
**Description:** `show information about your/someone else's clipboard`
<br/>
**Permissions:**

- `worldeditglobalizer.command.clipboard.info`
  <br/>

**Arguments:**

| Position |  Argument   | Description | Optional? | Boolean Flag? | Value Flag? | Default value |                    Permissions                     |
|:--------:|:-----------:|:-----------:|:---------:|:-------------:|:-----------:|:-------------:|:--------------------------------------------------:|
|    1     | player name |      -      |    yes    |      no       |     no      |               | `worldeditglobalizer.command.clipboard.info.other` |

---

### /weg clipboard upload

**Command:** `/weg clipboard upload`
<br/>
**Description:** `upload your clipboard`
<br/>
**Permissions:**

- `worldeditglobalizer.command.clipboard.upload`
  <br/>

**Arguments:** none

---

### /weg reload

**Command:** `/weg reload`
<br/>
**Description:** `reloads all configs`
<br/>
**Permissions:**

- `worldeditglobalizer.command.reload`
  <br/>

**Arguments:** none

---

### /weg schematic

**Command:** `/weg schematic`
<br/>
**Description:** `commands regarding schematics`
<br/>
**Permissions:** none
<br/>

**Arguments:** none

---

### /weg schematic delete <schematic name>

**Command:** `/weg schematic delete <schematic name>`
<br/>
**Description:** `deletes a schematic`
<br/>
**Permissions:**

- `worldeditglobalizer.command.schematic.delete`
  <br/>

**Arguments:**

| Position |    Argument    | Description | Optional? | Boolean Flag? | Value Flag? | Default value | Permissions |
|:--------:|:--------------:|:-----------:|:---------:|:-------------:|:-----------:|:-------------:|:-----------:|
|    1     | schematic name |      -      |    no     |      no       |     no      |               |             |

---

### /weg schematic list [-p: page]

**Command:** `/weg schematic list [-p: page]`
<br/>
**Description:** `a list of saved schematics`
<br/>
**Permissions:**

- `worldeditglobalizer.command.schematic.list`
  <br/>

**Arguments:**

| Position | Argument | Description | Optional? | Boolean Flag? | Value Flag? | Default value | Permissions |
|:--------:|:--------:|:-----------:|:---------:|:-------------:|:-----------:|:-------------:|:-----------:|
|    1     |   page   |      -      |    yes    |      no       |     yes     |               |             |

---

### /weg schematic load <schematic name>

**Command:** `/weg schematic load <schematic name>`
<br/>
**Description:** `load a schematic in yout clipboard`
<br/>
**Permissions:**

- `worldeditglobalizer.command.schematic.load`
  <br/>

**Arguments:**

| Position |    Argument    | Description | Optional? | Boolean Flag? | Value Flag? | Default value | Permissions |
|:--------:|:--------------:|:-----------:|:---------:|:-------------:|:-----------:|:-------------:|:-----------:|
|    1     | schematic name |      -      |    no     |      no       |     no      |               |             |

---

### /weg schematic save <schematic name>

**Command:** `/weg schematic save <schematic name>`
<br/>
**Description:** `save your clipboard to a schematic file`
<br/>
**Permissions:**

- `worldeditglobalizer.command.schematic.save`
  <br/>

**Arguments:**

| Position |    Argument    | Description | Optional? | Boolean Flag? | Value Flag? | Default value | Permissions |
|:--------:|:--------------:|:-----------:|:---------:|:-------------:|:-----------:|:-------------:|:-----------:|
|    1     | schematic name |      -      |    no     |      no       |     no      |               |             |

Permissions
-----------

| Permission                                          | Description                                     | Commands                                                                         |
| --------------------------------------------------- | ----------------------------------------------- | -------------------------------------------------------------------------------- |
| `worldeditglobalizer.admin.notify.update`           | allowing to receive update notifications        |                                                                                  |
| `worldeditglobalizer.clipboard.auto.download`       | enable clipboard auto-download                  |                                                                                  |
| `worldeditglobalizer.clipboard.auto.upload`         | enable clipboard auto-upload                    |                                                                                  |
| `worldeditglobalizer.command.clipboard.clear`       | -                                               | [`/weg clipboard clear [player name]`](#weg-clipboard-clear-player-name)         |
| `worldeditglobalizer.command.clipboard.clear.other` | allowing to clear clipboard of other players    | [`/weg clipboard clear [player name]`](#weg-clipboard-clear-player-name)         |
| `worldeditglobalizer.command.clipboard.download`    | -                                               | [`/weg clipboard download`](#weg-clipboard-download)                             |
| `worldeditglobalizer.command.clipboard.info`        | -                                               | [`/weg clipboard info [player name]`](#weg-clipboard-info-player-name)           |
| `worldeditglobalizer.command.clipboard.info.other`  | allowing to see clipboard info of other players | [`/weg clipboard info [player name]`](#weg-clipboard-info-player-name)           |
| `worldeditglobalizer.command.clipboard.upload`      | -                                               | [`/weg clipboard upload`](#weg-clipboard-upload)                                 |
| `worldeditglobalizer.command.reload`                | -                                               | [`/weg reload`](#weg-reload)                                                     |
| `worldeditglobalizer.command.schematic.delete`      | allowing to delete a schematic                  | [`/weg schematic delete <schematic name>`](#weg-schematic-delete-schematic-name) |
| `worldeditglobalizer.command.schematic.list`        | allowing to list saved schematics               | [`/weg schematic list [-p: page]`](#weg-schematic-list--p-page)                  |
| `worldeditglobalizer.command.schematic.load`        | allowing to load a schematic to a clipboard     | [`/weg schematic load <schematic name>`](#weg-schematic-load-schematic-name)     |
| `worldeditglobalizer.command.schematic.save`        | allowing to save a clipboard to a schematic     | [`/weg schematic save <schematic name>`](#weg-schematic-save-schematic-name)     |
