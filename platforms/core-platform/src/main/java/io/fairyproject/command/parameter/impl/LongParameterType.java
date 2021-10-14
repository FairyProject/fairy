/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.command.parameter.impl;

import io.fairyproject.bean.Component;
import io.fairyproject.command.CommandEvent;
import io.fairyproject.command.parameter.ParameterHolder;

@Component
public class LongParameterType implements ParameterHolder<Long> {

	@Override
	public Class[] type() {
		return new Class[] {Long.class, long.class};
	}

	public Long transform(CommandEvent event, String source) {
		if (source.toLowerCase().contains("e")) {
			event.sendInternalError(source + " is not a valid number.");
			return (null);
		}

		try {
			long parsed = Long.parseLong(source);

			if (Double.isNaN(parsed) || !Double.isFinite(parsed)) {
				event.sendInternalError(source + " is not a valid number.");
				return (null);
			}

			return (parsed);
		} catch (NumberFormatException exception) {
			event.sendInternalError(source + " is not a valid number.");
			return (null);
		}
	}

}