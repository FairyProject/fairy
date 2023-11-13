/*
 * Copyright 2012-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fairyproject.devtools.reload;

import io.fairyproject.container.InjectableComponent;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility to determine if a Java agent based reloader (e.g. JRebel) is being used.
 *
 * @author Phillip Webb
 */
@InjectableComponent
public class AgentDetector {

	private final Set<String> agentClasses;

	public AgentDetector() {
		Set<String> agentClasses = new LinkedHashSet<>();
		agentClasses.add("org.zeroturnaround.javarebel.Integration");
		agentClasses.add("org.zeroturnaround.javarebel.ReloaderFactory");
		agentClasses.add("org.hotswap.agent.HotswapAgent");
		this.agentClasses = Collections.unmodifiableSet(agentClasses);
	}

	/**
	 * Determine if any agent reloader is active.
	 * @return true if agent reloading is active
	 */
	public boolean isActive() {
		return isActive(null)
				|| isActive(AgentDetector.class.getClassLoader())
				|| isActive(ClassLoader.getSystemClassLoader());
	}

	private boolean isActive(ClassLoader classLoader) {
		for (String agentClass : agentClasses) {
			try {
				Class.forName(agentClass, false, classLoader);

				return true;
			} catch (ClassNotFoundException ex) {
				// Swallow and continue
			}
		}
		return false;
	}

}
