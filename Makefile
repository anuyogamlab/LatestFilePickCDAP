PLUGIN=SftpLatestFilePick
all:
	echo "install"

install:
	/home/anuyogam/Latest/CDAP/cdap-sandbox-6.3.0/bin/cdap cli load artifact ./target/$(PLUGIN)-1.1.0-SNAPSHOT.jar config-file ./target/$(PLUGIN)-1.1.0-SNAPSHOT.json

build:
	mvn package -DskipTests

run: build install
	@echo "Done"

stop:
	cdap sandbox stop

start:
	cdap sandbox start --enable-debug

start2:
	cdap sandbox start