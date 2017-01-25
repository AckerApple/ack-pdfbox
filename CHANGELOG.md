# ack-pdfbox - Change Log
All notable changes to this project will be documented here.

## [1.2.11] - 2017-01-17
### Added
- The begining of being able to run multiple CLI commands

## [1.2.8] - 2017-01-17
### Fix
- PDCheckBox being accidentally checked

## [1.2.7] - 2017-01-17
### Enhanced
- PDCheckBox handling

## [1.2.6] - 2017-01-17
### Enhanced
- during pdftoimage, PDFBox warnings are suppressed
- pdftoimage returns JSON array of relative-image-paths

## [1.2.4] - 2017-01-16
### Fix
- During acroform fill, XFA is set as to not be supported via acroform.setXFA(null)
### Added
- During acroform fill, option to flatten has been added

## [1.2.2] - 2017-01-10
### Fix
- add-image errored when width not present

## [1.2.0] - 2017-01-10
### Breaking Change
- add-image now works completely different. See README.md

## [1.1.4] - 2016-12-22
### Added
- sign

## [1.1.2] - 2016-12-07
### Added
- PDFToImage

## [1.1.0] - 2016-11-29
### Enhanced
- BouncyCastle is now bundled as part of master jar file
