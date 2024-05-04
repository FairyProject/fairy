# Run it in the root of the project

username=''
password=''
local=false

print_usage() {
  printf "Usage: ..."
}

deploy() {
  echo "Deploying..."
  if [ "$local" = true ]; then
    gradle publishToMavenLocal || exit &
    return
  else
    gradle publish || exit & #-PimanityLibrariesUsername=${{ secrets.MAVEN_USER }} -PimanityLibrariesPassword=${{ secrets.MAVEN_PASSWORD }}
  fi
}

while getopts 'abf:v' flag; do
  case "${flag}" in
    u) username="${OPTARG}" ;;
    p) password="${OPTARG}" ;;
    l) local=true ;;
    *) print_usage
       exit 1 ;;
  esac
done

cd gradle-plugin || exit
deploy

#cd ../shared || exit
#deploy

cd ../framework || exit
cd bom || exit
deploy

cd ../bootstraps || exit
deploy

cd ../bundles || exit
deploy

cd ../devtools || exit
deploy

cd ../modules || exit
cd core || exit
deploy

cd ../mc || exit
deploy

cd ../bukkit || exit
deploy

cd ../modules-bom || exit
deploy

cd ..
cd ../platforms || exit
deploy

cd ../tests || exit
deploy