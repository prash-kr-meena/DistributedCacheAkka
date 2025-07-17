project_dir="$(pwd)"

### For running on Terminal App
#osascript -e "tell application \"Terminal\" to do script \"cd '$project_dir'; mvn exec:java -Dexec.mainClass='com.meena.cache.DistributedCacheApp' -Dexec.args=2551\""
#osascript -e "tell application \"Terminal\" to do script \"cd '$project_dir'; mvn exec:java -Dexec.mainClass='com.meena.cache.DistributedCacheApp' -Dexec.args=2552\""
#osascript -e "tell application \"Terminal\" to do script \"cd '$project_dir'; mvn exec:java -Dexec.mainClass='com.meena.cache.DistributedCacheApp' -Dexec.args=0\""



### For running on ITerm App
#osascript -e "tell application \"iTerm\" to create window with default profile"
osascript -e "tell application \"iTerm\" to tell the current window to create tab with default profile"
osascript -e "tell application \"iTerm\" to tell the current session of current window to write text \"cd '$project_dir'; mvn exec:java -Dexec.mainClass='com.meena.cache.DistributedCacheApp' -Dexec.args=2551\""
osascript -e "tell application \"iTerm\" to tell the current window to create tab with default profile"
osascript -e "tell application \"iTerm\" to tell the current session of current window to write text \"cd '$project_dir'; mvn exec:java -Dexec.mainClass='com.meena.cache.DistributedCacheApp' -Dexec.args=2552\""
osascript -e "tell application \"iTerm\" to tell the current window to create tab with default profile"
osascript -e "tell application \"iTerm\" to tell the current session of current window to write text \"cd '$project_dir'; mvn exec:java -Dexec.mainClass='com.meena.cache.DistributedCacheApp' -Dexec.args=0\""