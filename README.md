# package2bundle
Converts FHIR package.tgz files into a FHIR Bundle

Main class is Package2Bundle. Takes an array of file paths to package.tgz files, and produces FHIR Bundle resources in the same directory with the name of the package file plus ".json" at the end (e.g. package.tgz.json).
