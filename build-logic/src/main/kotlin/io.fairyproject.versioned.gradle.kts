import util.getGlobalProperty

plugins {
    id("io.fairyproject.common")
    id("io.fairyproject.transformed")
}

version = getGlobalProperty("version")