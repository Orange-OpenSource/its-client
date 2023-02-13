PLANTUML_VERSION = 1.2023.1

#-------------------------------------------------------------------------------
PLANTUML_SITE = https://github.com/plantuml/plantuml/releases/download/v$(PLANTUML_VERSION)
PLANTUML_SOURCE = plantuml-$(PLANTUML_VERSION).jar
PLANTUML = $(dir $(lastword $(MAKEFILE_LIST)))$(PLANTUML_SOURCE)

MODELS = $(wildcard $(dir $(lastword $(MAKEFILE_LIST)))*.uml)

Q = @
ifeq ($(origin V),command line)
ifneq ($(V),)
Q =
endif
endif

# $(1): quiet command (e.g. PLANTUML)
# $(2): full command (e.g. pantuml $(*))
cmd = @ $(if $(Q),$(info $(1) $(@)),$(info $(2))) $(2)

all: $(patsubst %.uml,%.png,$(MODELS))
	@:

%.png: %.uml $(PLANTUML)
	$(call cmd,PLANTUML,java -Djava.awt.headless=true -jar $(PLANTUML) $(<))

$(PLANTUML):
	$(call cmd,WGET,wget $(if $(Q),-q) -O $(PLANTUML) '$(PLANTUML_SITE)/$(PLANTUML_SOURCE)')

clean:
	$(Q)rm -f $(patsubst %.uml,%.png,$(MODELS))

distclean: clean
	$(Q)rm -f $(PLANTUML)
